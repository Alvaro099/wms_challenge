package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "TaskResponse", description = "Replenishment task response")
public record TaskResponse(
        @Schema(description = "Task unique ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "SKU identifier", example = "SKU-100")
        String sku,

        @Schema(description = "Source reserve location code", example = "RSV-01")
        String fromLocation,

        @Schema(description = "Target picking location code", example = "PICK-01")
        String toLocation,

        @Schema(description = "Quantity to re-stock", example = "60")
        int quantity,

        @Schema(description = "Task status", example = "OPEN")
        TaskStatusDto status,

        @Schema(description = "Task creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Task last update timestamp")
        LocalDateTime updatedAt
) {
}
