package io.tenoro.app.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReplenishmentRule {
    private final String sku;
    private final String locationCode;
    private final int min;
    private final int max;

    public ReplenishmentRule(String sku, String locationCode, int min, int max) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (locationCode == null || locationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Location code cannot be null or empty");
        }
        if (min < 0) {
            throw new IllegalArgumentException("Min quantity cannot be negative");
        }
        if (max < min) {
            throw new IllegalArgumentException("Max quantity cannot be less than min quantity");
        }
        this.sku = sku.trim().toUpperCase();
        this.locationCode = locationCode.trim().toUpperCase();
        this.min = min;
        this.max = max;
    }
}
