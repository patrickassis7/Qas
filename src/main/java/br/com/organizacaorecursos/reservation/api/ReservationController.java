package br.com.organizacaorecursos.reservation.api;

import br.com.organizacaorecursos.reservation.api.dto.CreateReservationRequest;
import br.com.organizacaorecursos.reservation.api.dto.ReservationResponse;
import br.com.organizacaorecursos.reservation.api.dto.UpdateReservationRequest;
import br.com.organizacaorecursos.reservation.application.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMINISTRADOR";

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return reservationService.create(userId(authentication), request);
    }

    @GetMapping
    public Page<ReservationResponse> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return reservationService.list(userId(authentication), isAdmin(authentication), page, size);
    }

    @GetMapping("/{id}")
    public ReservationResponse findById(Authentication authentication, @PathVariable UUID id) {
        return reservationService.findById(id, userId(authentication), isAdmin(authentication));
    }

    @PatchMapping("/{id}")
    public ReservationResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReservationRequest request
    ) {
        return reservationService.update(id, userId(authentication), isAdmin(authentication), request);
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(Authentication authentication, @PathVariable UUID id) {
        return reservationService.cancel(id, userId(authentication), isAdmin(authentication));
    }

    private static UUID userId(Authentication authentication) {
        return UUID.fromString(((Jwt) authentication.getPrincipal()).getSubject());
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
    }
}
