package io.tenoro.app.domain.model;

import io.tenoro.app.domain.exception.IllegalTaskStateException;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
public class ReplenishmentTask {
    private final String id;
    private final String sku;
    private final String fromLocation;
    private final String toLocation;
    private final int quantity;
    private final ReplenishmentTaskStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ReplenishmentTask(String id, String sku, String fromLocation, String toLocation, int quantity, ReplenishmentTaskStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (fromLocation == null || fromLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("From location cannot be null or empty");
        }
        if (toLocation == null || toLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("To location cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.sku = sku.trim().toUpperCase();
        this.fromLocation = fromLocation.trim().toUpperCase();
        this.toLocation = toLocation.trim().toUpperCase();
        this.quantity = quantity;
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static ReplenishmentTask create(String sku, String fromLocation, String toLocation, int quantity) {
        LocalDateTime now = LocalDateTime.now();
        return new ReplenishmentTask(UUID.randomUUID().toString(), sku, fromLocation, toLocation, quantity, ReplenishmentTaskStatus.OPEN, now, now);
    }

    public ReplenishmentTask confirm() {
        if (this.status != ReplenishmentTaskStatus.OPEN) {
            throw new IllegalTaskStateException("Cannot confirm task in status: " + this.status);
        }
        return new ReplenishmentTask(this.id, this.sku, this.fromLocation, this.toLocation, this.quantity, ReplenishmentTaskStatus.CONFIRMED, this.createdAt, LocalDateTime.now());
    }

    public ReplenishmentTask cancel() {
        if (this.status != ReplenishmentTaskStatus.OPEN) {
            throw new IllegalTaskStateException("Cannot cancel task in status: " + this.status);
        }
        return new ReplenishmentTask(this.id, this.sku, this.fromLocation, this.toLocation, this.quantity, ReplenishmentTaskStatus.CANCELLED, this.createdAt, LocalDateTime.now());
    }
}
