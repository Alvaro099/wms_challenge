package io.tenoro.app.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class Location {
    private final String code;
    private final LocationType type;

    public Location(String code, LocationType type) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Location code cannot be null or empty");
        }
        this.code = code.trim().toUpperCase();
        this.type = Objects.requireNonNull(type, "Location type cannot be null");
    }

    public boolean isPicking() {
        return LocationType.PICKING.equals(type);
    }

    public boolean isReserve() {
        return LocationType.RESERVE.equals(type);
    }
}
