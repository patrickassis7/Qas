package br.com.organizacaorecursos.approval.api;

import br.com.organizacaorecursos.approval.api.dto.ReservationActionRequest;
import br.com.organizacaorecursos.approval.application.ApprovalService;
import br.com.organizacaorecursos.reservation.api.dto.ReservationResponse;
import br.com.organizacaorecursos.shared.infrastructure.web.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations/{id}")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/approve")
    public ReservationResponse approve(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ReservationActionRequest request
    ) {
        return approvalService.approve(id, CurrentUser.id(authentication), CurrentUser.isAdmin(authentication), note(request));
    }

    @PostMapping("/reject")
    public ReservationResponse reject(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ReservationActionRequest request
    ) {
        return approvalService.reject(id, CurrentUser.id(authentication), CurrentUser.isAdmin(authentication), note(request));
    }

    @PostMapping("/start")
    public ReservationResponse start(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ReservationActionRequest request
    ) {
        return approvalService.start(id, CurrentUser.id(authentication), CurrentUser.isAdmin(authentication), note(request));
    }

    @PostMapping("/complete")
    public ReservationResponse complete(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ReservationActionRequest request
    ) {
        return approvalService.complete(id, CurrentUser.id(authentication), CurrentUser.isAdmin(authentication), note(request));
    }

    @PostMapping("/no-show")
    public ReservationResponse noShow(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ReservationActionRequest request
    ) {
        return approvalService.noShow(id, CurrentUser.id(authentication), CurrentUser.isAdmin(authentication), note(request));
    }

    private static String note(ReservationActionRequest request) {
        return request == null ? null : request.note();
    }
}
