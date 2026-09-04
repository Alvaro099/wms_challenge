package io.tenoro.app.infra.adapter.inbound.web.mappers;

import io.tenoro.app.api.dto.TaskResponse;
import io.tenoro.app.domain.model.ReplenishmentTask;

public final class ReplenishmentTaskMapper {

    private ReplenishmentTaskMapper() {
    }

    public static TaskResponse toResponse(ReplenishmentTask task) {
        if (task == null) {
            return null;
        }
        return new TaskResponse(
                task.getId(),
                task.getSku(),
                task.getFromLocation(),
                task.getToLocation(),
                task.getQuantity(),
                EnumMapper.toDto(task.getStatus()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
