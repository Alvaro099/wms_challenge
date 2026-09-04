package io.tenoro.app.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Replenishment task status", enumAsRef = true)
public enum TaskStatusDto {
    OPEN,
    CONFIRMED,
    CANCELLED
}
