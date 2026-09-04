package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Warehouse location type", enumAsRef = true)
public enum LocationTypeDto {
    PICKING,
    RESERVE
}
