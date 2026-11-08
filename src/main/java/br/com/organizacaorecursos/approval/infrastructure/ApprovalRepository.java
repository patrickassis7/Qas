package br.com.organizacaorecursos.approval.infrastructure;

import br.com.organizacaorecursos.approval.domain.ApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApprovalRepository extends JpaRepository<ApprovalEntity, UUID> {
}
