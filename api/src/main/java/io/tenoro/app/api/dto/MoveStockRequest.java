package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "MoveStockRequest", description = "Payload to move stock between locations")
public record MoveStockRequest(
        @NotBlank
        @Schema(description = "SKU identifier", example = "SKU-100", requiredMode = Schema.RequiredMode.REQUIRED)
        String sku,

        @NotBlank
        @Schema(description = "Origin location code", example = "RSV-01", requiredMode = Schema.RequiredMode.REQUIRED)
        String from,

        @NotBlank
        @Schema(description = "Destination location code", example = "PICK-01", requiredMode = Schema.RequiredMode.REQUIRED)
        String to,

        @Min(1)
        @Schema(description = "Quantity to move", example = "20", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity
) {
}
