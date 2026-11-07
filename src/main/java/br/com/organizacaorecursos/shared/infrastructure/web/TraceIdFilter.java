package br.com.organizacaorecursos.shared.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String ATTRIBUTE = TraceIdFilter.class.getName() + ".traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();
        request.setAttribute(ATTRIBUTE, traceId);
        response.setHeader("X-Trace-Id", traceId);
        chain.doFilter(request, response);
    }

    public static String traceId(HttpServletRequest request) {
        Object value = request.getAttribute(ATTRIBUTE);
        return value == null ? UUID.randomUUID().toString() : value.toString();
    }
}
