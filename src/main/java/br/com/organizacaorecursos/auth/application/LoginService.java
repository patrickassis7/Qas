package br.com.organizacaorecursos.auth.application;

import br.com.organizacaorecursos.audit.application.AuditService;
import br.com.organizacaorecursos.auth.api.dto.LoginRequest;
import br.com.organizacaorecursos.auth.api.dto.LoginResponse;
import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class LoginService {

    private final AuthenticationManager manager;
    private final UserRepository users;
    private final JwtEncoder encoder;
    private final Duration lifetime;
    private final String issuer;
    private final AuditService audit;

    public LoginService(AuthenticationManager manager, UserRepository users, JwtEncoder encoder,
                        Duration jwtLifetime, @Value("${app.auth.issuer}") String issuer,
                        AuditService audit) {
        this.manager = manager;
        this.users = users;
        this.encoder = encoder;
        this.lifetime = jwtLifetime;
        this.issuer = issuer;
        this.audit = audit;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            manager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        UserEntity user = users.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }
        Instant now = Instant.now();
        Instant expiresAt = now.plus(lifetime);
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer)
                .subject(user.getId().toString()).issuedAt(now).expiresAt(expiresAt).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        audit.registrar(user.getId(), "LOGIN_SUCESSO", "USER", user.getId(), Map.of("result", "success"));
        return new LoginResponse(token, "Bearer", expiresAt);
    }
}
