package br.com.organizacaorecursos.shared.infrastructure.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class CurrentUser {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMINISTRADOR";

    private CurrentUser() {
    }

    public static UUID id(Authentication authentication) {
        return UUID.fromString(((Jwt) authentication.getPrincipal()).getSubject());
    }

    public static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
    }
}
