package io.tenoro.app.infra.adapter.inbound.web.mappers;

import io.tenoro.app.api.dto.LocationTypeDto;
import io.tenoro.app.api.dto.TaskStatusDto;
import io.tenoro.app.domain.model.LocationType;
import io.tenoro.app.domain.model.ReplenishmentTaskStatus;

public final class EnumMapper {

    private EnumMapper() {
    }

    public static LocationType toDomain(LocationTypeDto type) {
        return LocationType.valueOf(type.name());
    }

    public static LocationTypeDto toDto(LocationType type) {
        return LocationTypeDto.valueOf(type.name());
    }

    public static ReplenishmentTaskStatus toDomain(TaskStatusDto status) {
        return ReplenishmentTaskStatus.valueOf(status.name());
    }

    public static TaskStatusDto toDto(ReplenishmentTaskStatus status) {
        return TaskStatusDto.valueOf(status.name());
    }
}
