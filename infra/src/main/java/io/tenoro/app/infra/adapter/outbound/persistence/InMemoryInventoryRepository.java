package io.tenoro.app.infra.adapter.outbound.persistence;

import io.tenoro.app.domain.model.InventoryItem;
import io.tenoro.app.domain.port.outbound.InventoryRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<String, InventoryItem> inventory = new ConcurrentHashMap<>();

    private String buildKey(String sku, String locationCode) {
        return sku.toUpperCase() + ":" + locationCode.toUpperCase();
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        String key = buildKey(item.getSku(), item.getLocationCode());
        inventory.put(key, item);
        return item;
    }

    @Override
    public Optional<InventoryItem> findBySkuAndLocationCode(String sku, String locationCode) {
        if (sku == null || locationCode == null) return Optional.empty();
        String key = buildKey(sku, locationCode);
        return Optional.ofNullable(inventory.get(key));
    }

    @Override
    public List<InventoryItem> findBySku(String sku) {
        if (sku == null) return List.of();
        String searchSku = sku.toUpperCase();
        return inventory.values().stream()
                .filter(item -> item.getSku().equals(searchSku))
                .toList();
    }

    @Override
    public List<InventoryItem> findByLocationCode(String locationCode) {
        if (locationCode == null) return List.of();
        String searchLoc = locationCode.toUpperCase();
        return inventory.values().stream()
                .filter(item -> item.getLocationCode().equals(searchLoc))
                .toList();
    }

    @Override
    public List<InventoryItem> findAll() {
        return new ArrayList<>(inventory.values());
    }

    @Override
    public void deleteBySkuAndLocationCode(String sku, String locationCode) {
        if (sku != null && locationCode != null) {
            inventory.remove(buildKey(sku, locationCode));
        }
    }

    @Override
    public void deleteAll() {
        inventory.clear();
    }
}
