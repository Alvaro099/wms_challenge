package io.tenoro.app.domain.port.outbound;

import io.tenoro.app.domain.model.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> findBySkuAndLocationCode(String sku, String locationCode);
    List<InventoryItem> findBySku(String sku);
    List<InventoryItem> findByLocationCode(String locationCode);
    List<InventoryItem> findAll();
    void deleteBySkuAndLocationCode(String sku, String locationCode);
    void deleteAll();
}
