package br.com.organizacaorecursos.shared.infrastructure.config;

import br.com.organizacaorecursos.shared.api.ApiErrorResponse;
import br.com.organizacaorecursos.shared.infrastructure.web.TraceIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        write(request, response, 401, "UNAUTHORIZED", "Autenticação necessária ou inválida.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        write(request, response, 403, "FORBIDDEN", "Acesso não permitido para seu perfil.");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, int status,
                       String code, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
                code, message, List.of(), TraceIdFilter.traceId(request)));
    }
}
