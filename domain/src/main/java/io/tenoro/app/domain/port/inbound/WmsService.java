package io.tenoro.app.domain.port.inbound;

import io.tenoro.app.domain.model.*;

import java.util.List;
import java.util.Optional;

public interface WmsService {
    Location createLocation(String code, LocationType type);
    List<Location> getAllLocations();
    Optional<Location> getLocationByCode(String code);

    InventoryItem setStock(String sku, String locationCode, int quantity);
    List<InventoryItem> getStock(String sku, String locationCode);
    void moveStock(String sku, String fromLocation, String toLocation, int quantity);

    ReplenishmentRule createReplenishmentRule(String sku, String locationCode, int min, int max);
    List<ReplenishmentRule> getAllReplenishmentRules();

    List<ReplenishmentTask> generateReplenishmentTasks(String sku, String locationCode);
    List<ReplenishmentTask> scanAndGenerateAllReplenishmentTasks();
    List<ReplenishmentTask> getAllReplenishmentTasks(ReplenishmentTaskStatus status);
    ReplenishmentTask confirmTask(String id);
    ReplenishmentTask cancelTask(String id);
}
