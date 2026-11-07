package br.com.organizacaorecursos.auth.infrastructure;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public DatabaseUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
        String[] authorities = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .toArray(String[]::new);
        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .authorities(authorities)
                .build();
    }
}
