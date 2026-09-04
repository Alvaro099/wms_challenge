package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "GenerateTaskRequest", description = "Payload to evaluate and generate replenishment task")
public record GenerateTaskRequest(
        @NotBlank
        @Schema(description = "SKU identifier", example = "SKU-100", requiredMode = Schema.RequiredMode.REQUIRED)
        String sku,

        @NotBlank
        @Schema(description = "Picking location code", example = "PICK-01", requiredMode = Schema.RequiredMode.REQUIRED)
        String locationCode
) {
}
