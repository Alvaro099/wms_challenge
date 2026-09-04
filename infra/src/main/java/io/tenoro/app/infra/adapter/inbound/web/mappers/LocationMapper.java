package io.tenoro.app.infra.adapter.inbound.web.mappers;

import io.tenoro.app.api.dto.LocationResponse;
import io.tenoro.app.domain.model.Location;

public final class LocationMapper {

    private LocationMapper() {
    }

    public static LocationResponse toResponse(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationResponse(location.getCode(), EnumMapper.toDto(location.getType()));
    }
}
