package br.com.organizacaorecursos.approval.application;

import br.com.organizacaorecursos.approval.domain.ApprovalDecision;
import br.com.organizacaorecursos.approval.domain.ApprovalEntity;
import br.com.organizacaorecursos.approval.domain.WithdrawalRecordEntity;
import br.com.organizacaorecursos.approval.infrastructure.ApprovalRepository;
import br.com.organizacaorecursos.approval.infrastructure.WithdrawalRecordRepository;
import br.com.organizacaorecursos.audit.application.AuditService;
import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import br.com.organizacaorecursos.reservation.api.dto.ReservationResponse;
import br.com.organizacaorecursos.reservation.application.ReservationMapper;
import br.com.organizacaorecursos.reservation.application.ReservationNotAllowedException;
import br.com.organizacaorecursos.reservation.application.ReservationNotFoundException;
import br.com.organizacaorecursos.reservation.domain.ReservationEntity;
import br.com.organizacaorecursos.reservation.domain.ReservationStateMachine;
import br.com.organizacaorecursos.reservation.domain.ReservationStatus;
import br.com.organizacaorecursos.reservation.infrastructure.ReservationRepository;
import br.com.organizacaorecursos.resource.domain.ResourceEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ApprovalService {

    private static final String ENTITY = "RESERVATION";

    private final ReservationRepository reservations;
    private final ApprovalRepository approvals;
    private final WithdrawalRecordRepository withdrawals;
    private final UserRepository users;
    private final AuditService audit;
    private final ReservationMapper mapper;

    public ApprovalService(
            ReservationRepository reservations,
            ApprovalRepository approvals,
            WithdrawalRecordRepository withdrawals,
            UserRepository users,
            AuditService audit,
            ReservationMapper mapper
    ) {
        this.reservations = reservations;
        this.approvals = approvals;
        this.withdrawals = withdrawals;
        this.users = users;
        this.audit = audit;
        this.mapper = mapper;
    }

    public ReservationResponse approve(UUID id, UUID actorId, boolean admin, String note) {
        return decide(id, actorId, admin, note, ApprovalDecision.APROVADA,
                ReservationStatus.APROVADA, "RESERVA_APROVADA");
    }

    public ReservationResponse reject(UUID id, UUID actorId, boolean admin, String note) {
        return decide(id, actorId, admin, note, ApprovalDecision.REJEITADA,
                ReservationStatus.REJEITADA, "RESERVA_REJEITADA");
    }

    public ReservationResponse start(UUID id, UUID actorId, boolean admin, String note) {
        ReservationEntity reservation = load(id);
        requireOperator(reservation, actorId, admin);

        ReservationStatus previous = reservation.getStatus();
        reservation.transitionTo(ReservationStatus.EM_USO);
        Instant now = Instant.now();
        withdrawals.save(new WithdrawalRecordEntity(reservation, actor(actorId), now, note));

        Map<String, Object> details = transition(previous, reservation, note);
        details.put("withdrawnAt", now.toString());
        audit.registrar(actorId, "RESERVA_INICIADA", ENTITY, id, details);
        return mapper.toResponse(reservations.saveAndFlush(reservation));
    }

    public ReservationResponse complete(UUID id, UUID actorId, boolean admin, String note) {
        ReservationEntity reservation = load(id);
        requireOperator(reservation, actorId, admin);

        WithdrawalRecordEntity record = withdrawals.findByReservationId(id)
                .orElseThrow(() -> new ReservationNotAllowedException(
                        "A devolução exige uma retirada registrada."));

        ReservationStatus previous = reservation.getStatus();
        reservation.transitionTo(ReservationStatus.CONCLUIDA);
        Instant now = Instant.now();
        record.registerReturn(actor(actorId), now, note);
        withdrawals.save(record);

        Map<String, Object> details = transition(previous, reservation, note);
        details.put("returnedAt", now.toString());
        audit.registrar(actorId, "RESERVA_CONCLUIDA", ENTITY, id, details);
        return mapper.toResponse(reservations.saveAndFlush(reservation));
    }

    public ReservationResponse noShow(UUID id, UUID actorId, boolean admin, String note) {
        ReservationEntity reservation = load(id);
        requireOperator(reservation, actorId, admin);

        ReservationStatus previous = reservation.getStatus();
        ReservationStateMachine.requireTransition(previous, ReservationStatus.NAO_COMPARECEU);
        if (Instant.now().isBefore(reservation.getStartAt())) {
            throw new ReservationNotAllowedException(
                    "O não comparecimento só pode ser registrado após o horário de início da reserva.");
        }
        reservation.transitionTo(ReservationStatus.NAO_COMPARECEU);

        audit.registrar(actorId, "RESERVA_NAO_COMPARECEU", ENTITY, id, transition(previous, reservation, note));
        return mapper.toResponse(reservations.saveAndFlush(reservation));
    }

    private ReservationResponse decide(
            UUID id, UUID actorId, boolean admin, String note,
            ApprovalDecision decision, ReservationStatus target, String action
    ) {
        ReservationEntity reservation = load(id);
        if (reservation.getRequester().getId().equals(actorId)) {
            throw new AccessDeniedException("O solicitante não pode decidir a própria reserva.");
        }
        requireDecider(reservation, actorId, admin);

        ReservationStatus previous = reservation.getStatus();
        reservation.transitionTo(target);
        approvals.save(new ApprovalEntity(reservation, actor(actorId), decision, note));

        audit.registrar(actorId, action, ENTITY, id, transition(previous, reservation, note));
        return mapper.toResponse(reservations.saveAndFlush(reservation));
    }

    private ReservationEntity load(UUID id) {
        return reservations.findByIdForUpdate(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private UserEntity actor(UUID actorId) {
        return users.findById(actorId)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."));
    }

    // Decisão: administrador ou responsável de todos os recursos restritos da reserva.
    private static void requireDecider(ReservationEntity reservation, UUID actorId, boolean admin) {
        if (admin) {
            return;
        }
        boolean allowed = reservation.getResources().stream()
                .filter(ResourceEntity::isRestricted)
                .allMatch(resource -> isResponsible(resource, actorId))
                && reservation.getResources().stream().anyMatch(ResourceEntity::isRestricted);
        if (!allowed) {
            throw new AccessDeniedException("Apenas o responsável pelo recurso ou um administrador pode decidir.");
        }
    }

    // Retirada, devolução e não comparecimento: administrador ou responsável por algum recurso da reserva.
    private static void requireOperator(ReservationEntity reservation, UUID actorId, boolean admin) {
        if (admin || reservation.getResources().stream().anyMatch(resource -> isResponsible(resource, actorId))) {
            return;
        }
        throw new AccessDeniedException("Apenas o responsável pelo recurso ou um administrador pode registrar esta operação.");
    }

    private static boolean isResponsible(ResourceEntity resource, UUID actorId) {
        return resource.getResponsible() != null && resource.getResponsible().getId().equals(actorId);
    }

    private static Map<String, Object> transition(ReservationStatus previous, ReservationEntity reservation, String note) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("from", previous.name());
        details.put("to", reservation.getStatus().name());
        if (note != null) {
            details.put("note", note);
        }
        return details;
    }
}
