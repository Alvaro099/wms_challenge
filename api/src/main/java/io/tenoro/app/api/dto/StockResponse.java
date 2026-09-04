package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "StockResponse", description = "Stock item response")
public record StockResponse(
        @Schema(description = "SKU identifier", example = "SKU-100")
        String sku,

        @Schema(description = "Location code", example = "PICK-01")
        String locationCode,

        @Schema(description = "Quantity available", example = "5")
        int quantity
) {
}
