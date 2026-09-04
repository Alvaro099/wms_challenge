package io.tenoro.app.domain.support;

import io.tenoro.app.domain.model.InventoryItem;
import io.tenoro.app.domain.port.outbound.InventoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<String, InventoryItem> inventory = new ConcurrentHashMap<>();

    private String buildKey(String sku, String locationCode) {
        return sku.toUpperCase() + ":" + locationCode.toUpperCase();
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        inventory.put(buildKey(item.getSku(), item.getLocationCode()), item);
        return item;
    }

    @Override
    public Optional<InventoryItem> findBySkuAndLocationCode(String sku, String locationCode) {
        if (sku == null || locationCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(inventory.get(buildKey(sku, locationCode)));
    }

    @Override
    public List<InventoryItem> findBySku(String sku) {
        if (sku == null) {
            return List.of();
        }
        String searchSku = sku.toUpperCase();
        return inventory.values().stream()
                .filter(item -> item.getSku().equals(searchSku))
                .toList();
    }

    @Override
    public List<InventoryItem> findByLocationCode(String locationCode) {
        if (locationCode == null) {
            return List.of();
        }
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
