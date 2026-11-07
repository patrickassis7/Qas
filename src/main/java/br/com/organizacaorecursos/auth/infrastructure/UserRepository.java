package br.com.organizacaorecursos.auth.infrastructure;

import br.com.organizacaorecursos.auth.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findById(UUID id);
}
