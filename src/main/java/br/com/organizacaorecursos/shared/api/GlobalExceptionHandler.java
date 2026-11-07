package br.com.organizacaorecursos.shared.api;

import br.com.organizacaorecursos.auth.application.InvalidCredentialsException;
import br.com.organizacaorecursos.shared.infrastructure.web.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
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
                "FORBIDDEN", "Acesso não permitido para seu perfil.", List.of(), TraceIdFilter.traceId(request)));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> invalidCredentials(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(
                "UNAUTHORIZED", "Credenciais inválidas.", List.of(), TraceIdFilter.traceId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> invalidFields(MethodArgumentNotValidException exception,
                                                    HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(field -> new ApiErrorResponse.FieldError(field.getField(), field.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "VALIDATION_ERROR", "Dados inválidos.", errors, TraceIdFilter.traceId(request)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> malformedBody(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "INVALID_REQUEST", "Requisição inválida.", List.of(), TraceIdFilter.traceId(request)));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
                "NOT_FOUND", "A rota solicitada não foi encontrada.", List.of(), TraceIdFilter.traceId(request)));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                "INTERNAL_ERROR", "Não foi possível processar a solicitação.", List.of(),
                TraceIdFilter.traceId(request)));
    }
}
