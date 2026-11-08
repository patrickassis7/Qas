package br.com.organizacaorecursos.reservation.application;

import br.com.organizacaorecursos.audit.application.AuditService;
import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.reservation.api.dto.CreateReservationRequest;
import br.com.organizacaorecursos.reservation.api.dto.ReservationResponse;
import br.com.organizacaorecursos.reservation.api.dto.UpdateReservationRequest;
import br.com.organizacaorecursos.reservation.domain.ReservationEntity;
import br.com.organizacaorecursos.reservation.domain.ReservationStatus;
import br.com.organizacaorecursos.reservation.infrastructure.ReservationRepository;
import br.com.organizacaorecursos.resource.application.ResourceNotFoundException;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import br.com.organizacaorecursos.resource.domain.ResourceStatus;
import br.com.organizacaorecursos.resource.infrastructure.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationService {

    private static final String ENTITY = "RESERVATION";

    private final ReservationRepository reservations;
    private final ResourceRepository resources;
    private final UserRepository users;
    private final AgendaConflictChecker agenda;
    private final AuditService audit;
    private final ReservationMapper mapper;

    public ReservationService(
            ReservationRepository reservations,
            ResourceRepository resources,
            UserRepository users,
            AgendaConflictChecker agenda,
            AuditService audit,
            ReservationMapper mapper
    ) {
        this.reservations = reservations;
        this.resources = resources;
        this.users = users;
        this.agenda = agenda;
        this.audit = audit;
        this.mapper = mapper;
    }

    public ReservationResponse create(UUID actorId, CreateReservationRequest request) {
        requirePeriod(request.startAt(), request.endAt());

        UserEntity requester = users.findById(actorId)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."));
        List<ResourceEntity> locked = lockReservable(request.resourceIds());
        requireFreeAgenda(actorId, locked, request.startAt(), request.endAt(), null);

        ReservationEntity reservation = reservations.save(new ReservationEntity(
                requester, request.startAt(), request.endAt(), request.justification(), locked));
        audit.registrar(actorId, "RESERVA_CRIADA", ENTITY, reservation.getId(), details(reservation));

        approveAutomaticallyIfCommon(reservation, actorId);
        return mapper.toResponse(reservations.saveAndFlush(reservation));
    }

    public ReservationResponse update(UUID id, UUID actorId, boolean admin, UpdateReservationRequest request) {
        ReservationEntity reservation = findForUpdate(id);
        requireOwnerOrAdmin(reservation, actorId, admin);

        if (reservation.getStatus() != ReservationStatus.SOLICITADA
                && reservation.getStatus() != ReservationStatus.APROVADA) {
            throw new ReservationNotAllowedException(
                    "Reserva no estado " + reservation.getStatus() + " não pode ser alterada.");
        }

        Instant startAt = request.startAt() != null ? request.startAt() : reservation.getStartAt();
        Instant endAt = request.endAt() != null ? request.endAt() : reservation.getEndAt();
        String justification = request.justification() != null
                ? request.justification() : reservation.getJustification();
        Set<UUID> resourceIds = request.resourceIds() != null
                ? request.resourceIds()
                : reservation.getResources().stream().map(ResourceEntity::getId)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        requirePeriod(startAt, endAt);
        List<ResourceEntity> locked = lockReservable(resourceIds);

        if (reservation.getStatus() == ReservationStatus.APROVADA
                && locked.stream().anyMatch(ResourceEntity::isRestricted)) {
            throw new ReservationNotAllowedException(
                    "Reserva aprovada com recurso restrito não pode ser alterada; cancele e solicite novamente.");
        }

        requireFreeAgenda(actorId, locked, startAt, endAt, reservation.getId());

        reservation.reschedule(startAt, endAt, justification, locked);
        audit.registrar(actorId, "RESERVA_ALTERADA", ENTITY, reservation.getId(), details(reservation));

        approveAutomaticallyIfCommon(reservation, actorId);
        return mapper.toResponse(reservations.saveAndFlush(reservation));
    }

    public ReservationResponse cancel(UUID id, UUID actorId, boolean admin) {
        ReservationEntity reservation = findForUpdate(id);
        requireOwnerOrAdmin(reservation, actorId, admin);

        reservation.transitionTo(ReservationStatus.CANCELADA);
        audit.registrar(actorId, "RESERVA_CANCELADA", ENTITY, reservation.getId(), details(reservation));
        return mapper.toResponse(reservations.saveAndFlush(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(UUID id, UUID actorId, boolean admin) {
        ReservationEntity reservation = find(id);
        requireOwnerOrAdmin(reservation, actorId, admin);
        return mapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> list(UUID actorId, boolean admin, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startAt").descending());
        Page<ReservationEntity> result = admin
                ? reservations.findAll(pageable)
                : reservations.findByRequesterId(actorId, pageable);
        return result.map(mapper::toResponse);
    }

    private ReservationEntity find(UUID id) {
        return reservations.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private ReservationEntity findForUpdate(UUID id) {
        return reservations.findByIdForUpdate(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private static void requireOwnerOrAdmin(ReservationEntity reservation, UUID actorId, boolean admin) {
        if (!admin && !reservation.getRequester().getId().equals(actorId)) {
            throw new AccessDeniedException("Reserva pertence a outro usuário.");
        }
    }

    private static void requirePeriod(Instant startAt, Instant endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new InvalidReservationPeriodException("O fim da reserva deve ser posterior ao início.");
        }
    }

    private List<ResourceEntity> lockReservable(Set<UUID> ids) {
        List<ResourceEntity> found = resources.findAllByIdForUpdate(ids);
        if (found.size() != ids.size()) {
            Set<UUID> foundIds = new LinkedHashSet<>();
            found.forEach(resource -> foundIds.add(resource.getId()));
            ids.stream().filter(id -> !foundIds.contains(id)).findFirst()
                    .ifPresent(missing -> {
                        throw new ResourceNotFoundException(missing);
                    });
        }
        found.stream().filter(resource -> resource.getStatus() != ResourceStatus.ATIVO).findFirst()
                .ifPresent(resource -> {
                    throw new ReservationNotAllowedException(
                            "O recurso '" + resource.getName() + "' não está disponível para reserva.");
                });
        return found;
    }

    private void requireFreeAgenda(
            UUID actorId,
            List<ResourceEntity> targets,
            Instant startAt,
            Instant endAt,
            UUID ignoredReservationId
    ) {
        List<String> conflicts = new ArrayList<>();
        for (ResourceEntity resource : targets) {
            if (agenda.isReserved(resource.getId(), startAt, endAt, ignoredReservationId)) {
                conflicts.add("recurso '" + resource.getName() + "' já reservado");
            }
            if (agenda.isUnderMaintenance(resource.getId(), startAt, endAt)) {
                conflicts.add("recurso '" + resource.getName() + "' em manutenção");
            }
        }
        if (conflicts.isEmpty()) {
            return;
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("startAt", startAt.toString());
        details.put("endAt", endAt.toString());
        details.put("resourceIds", targets.stream().map(resource -> resource.getId().toString()).toList());
        details.put("conflicts", conflicts);
        audit.registrarIndependente(actorId, "CONFLITO_EVITADO", ENTITY, ignoredReservationId, details);

        throw new ReservationConflictException(conflicts);
    }

    private void approveAutomaticallyIfCommon(ReservationEntity reservation, UUID actorId) {
        boolean common = reservation.getResources().stream().noneMatch(ResourceEntity::isRestricted);
        if (common && reservation.getStatus() == ReservationStatus.SOLICITADA) {
            reservation.transitionTo(ReservationStatus.APROVADA);
            Map<String, Object> details = details(reservation);
            details.put("mode", "AUTOMATICA");
            audit.registrar(actorId, "RESERVA_APROVADA_AUTOMATICAMENTE", ENTITY, reservation.getId(), details);
        }
    }

    private static Map<String, Object> details(ReservationEntity reservation) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", reservation.getStatus().name());
        details.put("startAt", reservation.getStartAt().toString());
        details.put("endAt", reservation.getEndAt().toString());
        details.put("resourceIds", reservation.getResources().stream()
                .map(resource -> resource.getId().toString()).sorted().toList());
        return details;
    }
}
