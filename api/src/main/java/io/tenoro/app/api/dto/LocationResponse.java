package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LocationResponse", description = "Warehouse location response")
public record LocationResponse(
        @Schema(description = "Location unique code", example = "PICK-01")
        String code,

        @Schema(description = "Location type", example = "PICKING")
        LocationTypeDto type
) {
}
