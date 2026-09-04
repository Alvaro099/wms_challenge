package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response containing error details")
public record ErrorResponse(
        @Schema(description = "Error message describing what went wrong", example = "Invalid input data")
        String message,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "Timestamp of the error", example = "2023-09-12T10:30:00Z")
        String timestamp
) {
}
