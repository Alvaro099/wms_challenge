package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "SetStockRequest", description = "Payload to set stock quantity of a SKU in a location")
public record SetStockRequest(
        @NotBlank
        @Schema(description = "SKU identifier", example = "SKU-100", requiredMode = Schema.RequiredMode.REQUIRED)
        String sku,

        @NotBlank
        @Schema(description = "Location code", example = "PICK-01", requiredMode = Schema.RequiredMode.REQUIRED)
        String locationCode,

        @Min(0)
        @Schema(description = "Quantity available", example = "5", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity
) {
}
