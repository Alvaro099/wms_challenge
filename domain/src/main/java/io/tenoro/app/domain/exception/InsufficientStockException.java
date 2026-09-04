package io.tenoro.app.domain.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, String locationCode, int requested, int available) {
        super(String.format("Insufficient stock for SKU '%s' at location '%s': requested %d, available %d",
                sku, locationCode, requested, available));
    }
}
