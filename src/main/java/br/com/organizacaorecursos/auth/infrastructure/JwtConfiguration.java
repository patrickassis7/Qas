package br.com.organizacaorecursos.auth.infrastructure;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.time.Duration;

@Configuration
public class JwtConfiguration {

    @Bean
    SecretKey jwtSigningKey() {
        return decodeSigningKey(System.getenv("JWT_SECRET"));
    }

    static SecretKey decodeSigningKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("JWT_SECRET deve ser configurada no ambiente");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET deve estar em Base64 padrão válido");
        }
        if (!Base64.getEncoder().encodeToString(bytes).equals(value)) {
            throw new IllegalStateException("JWT_SECRET deve estar em Base64 padrão válido");
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve representar pelo menos 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSigningKey,
                          @Value("${app.auth.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> standard = JwtValidators.createDefaultWithIssuer(issuer);
        decoder.setJwtValidator(token -> {
            OAuth2TokenValidatorResult result = standard.validate(token);
            if (result.hasErrors()) {
                return result;
            }
            if (token.getIssuedAt() == null || token.getExpiresAt() == null
                    || token.getExpiresAt().isAfter(token.getIssuedAt().plus(Duration.ofHours(1)))) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
            }
            return OAuth2TokenValidatorResult.success();
        });
        return decoder;
    }

    @Bean
    Duration jwtLifetime(@Value("${app.auth.ttl-minutes}") long minutes) {
        if (minutes < 1 || minutes > 60) {
            throw new IllegalStateException("JWT_TTL_MINUTES deve estar entre 1 e 60");
        }
        return Duration.ofMinutes(minutes);
    }
}
