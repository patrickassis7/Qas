package br.com.organizacaorecursos.shared.api;

import br.com.organizacaorecursos.auth.application.InvalidCredentialsException;
import br.com.organizacaorecursos.audit.application.InvalidAuditQueryException;
import br.com.organizacaorecursos.maintenance.application.InvalidMaintenancePeriodException;
import br.com.organizacaorecursos.maintenance.application.MaintenanceOverlapException;
import br.com.organizacaorecursos.reservation.application.InvalidReservationPeriodException;
import br.com.organizacaorecursos.report.application.InvalidReportPeriodException;
import br.com.organizacaorecursos.reservation.application.ReservationConflictException;
import br.com.organizacaorecursos.reservation.application.ReservationNotAllowedException;
import br.com.organizacaorecursos.reservation.application.ReservationNotFoundException;
import br.com.organizacaorecursos.reservation.domain.InvalidReservationTransitionException;
import br.com.organizacaorecursos.resource.application.ResourceAlreadyExistsException;
import br.com.organizacaorecursos.resource.application.ResourceNotFoundException;
import br.com.organizacaorecursos.shared.infrastructure.web.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> accessDenied(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiErrorResponse(
                "FORBIDDEN",
                "Acesso não permitido para seu perfil.",
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> invalidCredentials(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(
                "UNAUTHORIZED",
                "Credenciais inválidas.",
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

        @ExceptionHandler(InvalidAuditQueryException.class)
        ResponseEntity<ApiErrorResponse> invalidAuditQuery(
                        InvalidAuditQueryException exception,
                        HttpServletRequest request
        ) {
                return error(HttpStatus.BAD_REQUEST, "INVALID_AUDIT_QUERY", exception.getMessage(), request);
        }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> resourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    ResponseEntity<ApiErrorResponse> resourceAlreadyExists(
            ResourceAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
                "RESOURCE_ALREADY_EXISTS",
                exception.getMessage(),
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(InvalidMaintenancePeriodException.class)
    ResponseEntity<ApiErrorResponse> invalidMaintenancePeriod(
            InvalidMaintenancePeriodException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(
                "INVALID_MAINTENANCE_PERIOD",
                exception.getMessage(),
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(MaintenanceOverlapException.class)
    ResponseEntity<ApiErrorResponse> maintenanceOverlap(
            MaintenanceOverlapException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
                "MAINTENANCE_OVERLAP",
                exception.getMessage(),
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(ReservationConflictException.class)
    ResponseEntity<ApiErrorResponse> reservationConflict(
            ReservationConflictException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, "RESERVATION_CONFLICT", exception.getMessage(), request);
    }

    @ExceptionHandler(ReservationNotAllowedException.class)
    ResponseEntity<ApiErrorResponse> reservationNotAllowed(
            ReservationNotAllowedException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, "RESERVATION_NOT_ALLOWED", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidReservationTransitionException.class)
    ResponseEntity<ApiErrorResponse> invalidReservationTransition(
            InvalidReservationTransitionException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, "INVALID_RESERVATION_TRANSITION", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidReservationPeriodException.class)
    ResponseEntity<ApiErrorResponse> invalidReservationPeriod(
            InvalidReservationPeriodException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_RESERVATION_PERIOD", exception.getMessage(), request);
    }

        @ExceptionHandler(InvalidReportPeriodException.class)
        ResponseEntity<ApiErrorResponse> invalidReportPeriod(
                        InvalidReportPeriodException exception,
                        HttpServletRequest request
        ) {
                return error(HttpStatus.BAD_REQUEST, "INVALID_REPORT_PERIOD", exception.getMessage(), request);
        }

    @ExceptionHandler(ReservationNotFoundException.class)
    ResponseEntity<ApiErrorResponse> reservationNotFound(
            ReservationNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", exception.getMessage(), request);
    }

    private static ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                code,
                message,
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> invalidFields(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ApiErrorResponse.FieldError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(field -> new ApiErrorResponse.FieldError(
                        field.getField(),
                        field.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "VALIDATION_ERROR",
                "Dados inválidos.",
                errors,
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> malformedBody(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "INVALID_REQUEST",
                "Requisição inválida.",
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiErrorResponse> methodNotAllowed(HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "Método HTTP não permitido para esta rota.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
                "NOT_FOUND",
                "A rota solicitada não foi encontrada.",
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                "INTERNAL_ERROR",
                "Não foi possível processar a solicitação.",
                List.of(),
                TraceIdFilter.traceId(request)
        ));
    }
}