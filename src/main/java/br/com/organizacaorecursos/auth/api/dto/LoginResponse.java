package br.com.organizacaorecursos.auth.api.dto;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt) {
}
