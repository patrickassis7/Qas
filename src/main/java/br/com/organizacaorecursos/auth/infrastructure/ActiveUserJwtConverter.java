package br.com.organizacaorecursos.auth.infrastructure;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ActiveUserJwtConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private final UserRepository users;

    public ActiveUserJwtConverter(UserRepository users) {
        this.users = users;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        UUID id;
        try {
            id = UUID.fromString(jwt.getSubject());
        } catch (RuntimeException exception) {
            throw invalidToken();
        }
        UserEntity user = users.findById(id).filter(UserEntity::isActive)
                .orElseThrow(ActiveUserJwtConverter::invalidToken);
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();
        return new JwtAuthenticationToken(jwt, authorities, user.getEmail());
    }

    private static OAuth2AuthenticationException invalidToken() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token"));
    }
}
