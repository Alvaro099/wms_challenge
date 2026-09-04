package io.tenoro.app.domain.service;

import io.tenoro.app.domain.exception.*;
import io.tenoro.app.domain.model.*;
import io.tenoro.app.domain.port.inbound.WmsService;
import io.tenoro.app.domain.port.outbound.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WmsDomainService implements WmsService {

    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;
    private final ReplenishmentRuleRepository replenishmentRuleRepository;
    private final ReplenishmentTaskRepository replenishmentTaskRepository;

    public WmsDomainService(LocationRepository locationRepository,
                            InventoryRepository inventoryRepository,
                            ReplenishmentRuleRepository replenishmentRuleRepository,
                            ReplenishmentTaskRepository replenishmentTaskRepository) {
        this.locationRepository = locationRepository;
        this.inventoryRepository = inventoryRepository;
        this.replenishmentRuleRepository = replenishmentRuleRepository;
        this.replenishmentTaskRepository = replenishmentTaskRepository;
    }

    @Override
    public Location createLocation(String code, LocationType type) {
        String normalizedCode = code != null ? code.trim().toUpperCase() : null;
        if (normalizedCode != null && locationRepository.existsByCode(normalizedCode)) {
            throw new DuplicateEntityException("Location already exists with code: " + normalizedCode);
        }
        Location location = new Location(code, type);
        return locationRepository.save(location);
    }

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public Optional<Location> getLocationByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return locationRepository.findByCode(code.trim().toUpperCase());
    }

    @Override
    public InventoryItem setStock(String sku, String locationCode, int quantity) {
        String normalizedLoc = locationCode != null ? locationCode.trim().toUpperCase() : null;
        if (normalizedLoc == null || !locationRepository.existsByCode(normalizedLoc)) {
            throw new LocationNotFoundException(locationCode);
        }
        InventoryItem item = new InventoryItem(sku, normalizedLoc, quantity);
        return inventoryRepository.save(item);
    }

    @Override
    public List<InventoryItem> getStock(String sku, String locationCode) {
        String normalizedSku = sku != null && !sku.trim().isEmpty() ? sku.trim().toUpperCase() : null;
        String normalizedLoc = locationCode != null && !locationCode.trim().isEmpty() ? locationCode.trim().toUpperCase() : null;

        if (normalizedSku != null && normalizedLoc != null) {
            return inventoryRepository.findBySkuAndLocationCode(normalizedSku, normalizedLoc)
                    .map(List::of)
                    .orElse(List.of());
        } else if (normalizedSku != null) {
            return inventoryRepository.findBySku(normalizedSku);
        } else if (normalizedLoc != null) {
            return inventoryRepository.findByLocationCode(normalizedLoc);
        } else {
            return inventoryRepository.findAll();
        }
    }

    @Override
    public synchronized void moveStock(String sku, String fromLocation, String toLocation, int quantity) {
        if (quantity <= 0) {
            throw new InvalidDomainRuleException("Move quantity must be greater than zero");
        }
        String normalizedSku = sku.trim().toUpperCase();
        String normalizedFrom = fromLocation.trim().toUpperCase();
        String normalizedTo = toLocation.trim().toUpperCase();

        locationRepository.findByCode(normalizedFrom)
                .orElseThrow(() -> new LocationNotFoundException(normalizedFrom));
        locationRepository.findByCode(normalizedTo)
                .orElseThrow(() -> new LocationNotFoundException(normalizedTo));

        InventoryItem fromItem = inventoryRepository.findBySkuAndLocationCode(normalizedSku, normalizedFrom)
                .orElse(new InventoryItem(normalizedSku, normalizedFrom, 0));

        if (fromItem.getQuantity() < quantity) {
            throw new InsufficientStockException(normalizedSku, normalizedFrom, quantity, fromItem.getQuantity());
        }

        InventoryItem toItem = inventoryRepository.findBySkuAndLocationCode(normalizedSku, normalizedTo)
                .orElse(new InventoryItem(normalizedSku, normalizedTo, 0));

        // Atomic update of both locations
        inventoryRepository.save(fromItem.withQuantity(fromItem.getQuantity() - quantity));
        inventoryRepository.save(toItem.withQuantity(toItem.getQuantity() + quantity));
    }

    @Override
    public ReplenishmentRule createReplenishmentRule(String sku, String locationCode, int min, int max) {
        String normalizedLoc = locationCode != null ? locationCode.trim().toUpperCase() : null;
        Location location = locationRepository.findByCode(normalizedLoc)
                .orElseThrow(() -> new LocationNotFoundException(locationCode));

        if (!location.isPicking()) {
            throw new InvalidDomainRuleException("Replenishment rule location must be of type PICKING. Provided: " + location.getType());
        }

        String normalizedSku = sku != null ? sku.trim().toUpperCase() : null;
        if (replenishmentRuleRepository.existsBySkuAndLocationCode(normalizedSku, normalizedLoc)) {
            throw new DuplicateEntityException(String.format("Replenishment rule already exists for SKU '%s' at location '%s'", normalizedSku, normalizedLoc));
        }

        ReplenishmentRule rule = new ReplenishmentRule(sku, locationCode, min, max);
        return replenishmentRuleRepository.save(rule);
    }

    @Override
    public List<ReplenishmentRule> getAllReplenishmentRules() {
        return replenishmentRuleRepository.findAll();
    }

    @Override
    public synchronized List<ReplenishmentTask> generateReplenishmentTasks(String sku, String locationCode) {
        String normalizedSku = sku.trim().toUpperCase();
        String normalizedLoc = locationCode.trim().toUpperCase();

        Location location = locationRepository.findByCode(normalizedLoc)
                .orElseThrow(() -> new LocationNotFoundException(normalizedLoc));

        if (!location.isPicking()) {
            throw new InvalidDomainRuleException("Target location must be of type PICKING for replenishment. Provided: " + location.getType());
        }

        ReplenishmentRule rule = replenishmentRuleRepository.findBySkuAndLocationCode(normalizedSku, normalizedLoc)
                .orElseThrow(() -> new InvalidDomainRuleException(String.format("No replenishment rule found for SKU '%s' at location '%s'", normalizedSku, normalizedLoc)));

        int currentStock = inventoryRepository.findBySkuAndLocationCode(normalizedSku, normalizedLoc)
                .map(InventoryItem::getQuantity)
                .orElse(0);

        int pendingIncoming = replenishmentTaskRepository.findBySkuAndToLocation(normalizedSku, normalizedLoc).stream()
                .filter(t -> t.getStatus() == ReplenishmentTaskStatus.OPEN)
                .mapToInt(ReplenishmentTask::getQuantity)
                .sum();

        int effectiveStock = currentStock + pendingIncoming;

        if (effectiveStock >= rule.getMin()) {
            return List.of();
        }

        int needed = rule.getMax() - effectiveStock;

        List<InventoryItem> reserveItems = inventoryRepository.findBySku(normalizedSku).stream()
                .filter(item -> {
                    Optional<Location> loc = locationRepository.findByCode(item.getLocationCode());
                    return loc.isPresent() && loc.get().isReserve() && item.getQuantity() > 0;
                })
                .toList();

        List<ReplenishmentTask> generatedTasks = new ArrayList<>();

        for (InventoryItem reserveItem : reserveItems) {
            if (needed <= 0) {
                break;
            }
            int qtyToTake = Math.min(reserveItem.getQuantity(), needed);
            if (qtyToTake > 0) {
                ReplenishmentTask task = ReplenishmentTask.create(normalizedSku, reserveItem.getLocationCode(), normalizedLoc, qtyToTake);
                ReplenishmentTask savedTask = replenishmentTaskRepository.save(task);
                generatedTasks.add(savedTask);
                needed -= qtyToTake;
            }
        }

        return generatedTasks;
    }

    @Override
    public List<ReplenishmentTask> scanAndGenerateAllReplenishmentTasks() {
        List<ReplenishmentRule> rules = replenishmentRuleRepository.findAll();
        List<ReplenishmentTask> allGenerated = new ArrayList<>();
        for (ReplenishmentRule rule : rules) {
            List<ReplenishmentTask> tasks = generateReplenishmentTasks(rule.getSku(), rule.getLocationCode());
            allGenerated.addAll(tasks);
        }
        return allGenerated;
    }

    @Override
    public List<ReplenishmentTask> getAllReplenishmentTasks(ReplenishmentTaskStatus status) {
        if (status != null) {
            return replenishmentTaskRepository.findByStatus(status);
        }
        return replenishmentTaskRepository.findAll();
    }

    @Override
    public synchronized ReplenishmentTask confirmTask(String id) {
        ReplenishmentTask task = replenishmentTaskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        ReplenishmentTask confirmedTask = task.confirm();
        moveStock(task.getSku(), task.getFromLocation(), task.getToLocation(), task.getQuantity());
        return replenishmentTaskRepository.save(confirmedTask);
    }

    @Override
    public ReplenishmentTask cancelTask(String id) {
        ReplenishmentTask task = replenishmentTaskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        ReplenishmentTask cancelledTask = task.cancel();
        return replenishmentTaskRepository.save(cancelledTask);
    }
}
