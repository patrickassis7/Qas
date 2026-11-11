package br.com.organizacaorecursos.maintenance.application;

import br.com.organizacaorecursos.maintenance.api.dto.MaintenanceBlockResponse;
import br.com.organizacaorecursos.maintenance.domain.MaintenanceBlockEntity;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceMapper {

    public MaintenanceBlockResponse toResponse(
            MaintenanceBlockEntity block
    ) {
        return new MaintenanceBlockResponse(
                block.getId(),
                block.getResource().getId(),
                block.getStartAt(),
                block.getEndAt(),
                block.getReason(),
                block.getCreatedBy().getId(),
                block.getStatus(),
                block.getCreatedAt()
        );
    }
}