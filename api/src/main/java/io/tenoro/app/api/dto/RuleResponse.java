package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RuleResponse", description = "Replenishment rule response")
public record RuleResponse(
        @Schema(description = "SKU identifier", example = "SKU-100")
        String sku,

        @Schema(description = "Picking location code", example = "PICK-01")
        String locationCode,

        @Schema(description = "Minimum threshold", example = "20")
        int min,

        @Schema(description = "Maximum threshold", example = "100")
        int max
) {
}
