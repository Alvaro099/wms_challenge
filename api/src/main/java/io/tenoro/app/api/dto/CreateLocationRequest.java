package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateLocationRequest", description = "Payload to create a warehouse location")
public record CreateLocationRequest(
        @NotBlank
        @Schema(description = "Unique location code", example = "PICK-01", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @NotNull
        @Schema(description = "Location type", example = "PICKING", requiredMode = Schema.RequiredMode.REQUIRED)
        LocationTypeDto type
) {
}
