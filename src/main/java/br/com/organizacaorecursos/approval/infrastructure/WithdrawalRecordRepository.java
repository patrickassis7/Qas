package br.com.organizacaorecursos.approval.infrastructure;

import br.com.organizacaorecursos.approval.domain.WithdrawalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WithdrawalRecordRepository extends JpaRepository<WithdrawalRecordEntity, UUID> {

    Optional<WithdrawalRecordEntity> findByReservationId(UUID reservationId);
}
