package io.tenoro.app.infra.adapter.outbound.persistence;

import io.tenoro.app.domain.model.ReplenishmentTask;
import io.tenoro.app.domain.model.ReplenishmentTaskStatus;
import io.tenoro.app.domain.port.outbound.ReplenishmentTaskRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryReplenishmentTaskRepository implements ReplenishmentTaskRepository {
    private final Map<String, ReplenishmentTask> tasks = new ConcurrentHashMap<>();

    @Override
    public ReplenishmentTask save(ReplenishmentTask task) {
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<ReplenishmentTask> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<ReplenishmentTask> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<ReplenishmentTask> findByStatus(ReplenishmentTaskStatus status) {
        if (status == null) return findAll();
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .toList();
    }

    @Override
    public List<ReplenishmentTask> findBySkuAndToLocation(String sku, String toLocation) {
        if (sku == null || toLocation == null) return List.of();
        String searchSku = sku.toUpperCase();
        String searchTo = toLocation.toUpperCase();
        return tasks.values().stream()
                .filter(task -> task.getSku().equals(searchSku) && task.getToLocation().equals(searchTo))
                .toList();
    }

    @Override
    public void deleteAll() {
        tasks.clear();
    }
}
