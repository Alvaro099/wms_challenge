package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateRuleRequest", description = "Payload to create a replenishment rule")
public record CreateRuleRequest(
        @NotBlank
        @Schema(description = "SKU identifier", example = "SKU-100", requiredMode = Schema.RequiredMode.REQUIRED)
        String sku,

        @NotBlank
        @Schema(description = "Picking location code", example = "PICK-01", requiredMode = Schema.RequiredMode.REQUIRED)
        String locationCode,

        @Min(0)
        @Schema(description = "Minimum stock threshold", example = "20", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int min,

        @Min(0)
        @Schema(description = "Maximum stock threshold", example = "100", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int max
) {
}
