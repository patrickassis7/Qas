package br.com.organizacaorecursos.auth.application;

import br.com.organizacaorecursos.auth.domain.RoleEntity;
import br.com.organizacaorecursos.auth.domain.UserEntity;
import br.com.organizacaorecursos.auth.infrastructure.RoleRepository;
import br.com.organizacaorecursos.auth.infrastructure.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@Profile("local")
@Order(1)
public class LocalIdentitySeeder implements CommandLineRunner {

    private static final List<SeedUser> USERS = List.of(
            new SeedUser("Professor Local", "professor.local@organizacao-recursos.local", "SOLICITANTE"),
            new SeedUser("Responsável Local", "responsavel.local@organizacao-recursos.local", "RESPONSAVEL"),
            new SeedUser("Administrador Local", "admin.local@organizacao-recursos.local", "ADMINISTRADOR")
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LocalIdentitySeeder(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (SeedUser seedUser : USERS) {
            if (userRepository.findByEmail(seedUser.email()).isPresent()) {
                continue;
            }

            RoleEntity role = roleRepository.findByName(seedUser.role())
                    .orElseThrow(() -> new IllegalStateException("Papel de seed não encontrado: " + seedUser.role()));

            UserEntity user = new UserEntity(
                    seedUser.name(),
                    seedUser.email(),
                    passwordEncoder.encode(UUID.randomUUID().toString()),
                    true
            );
            user.addRole(role);
            userRepository.save(user);
        }
    }

    private record SeedUser(String name, String email, String role) {
    }
}
