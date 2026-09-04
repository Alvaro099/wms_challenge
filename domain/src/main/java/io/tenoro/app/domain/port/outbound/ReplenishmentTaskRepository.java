package io.tenoro.app.domain.port.outbound;

import io.tenoro.app.domain.model.ReplenishmentTask;
import io.tenoro.app.domain.model.ReplenishmentTaskStatus;

import java.util.List;
import java.util.Optional;

public interface ReplenishmentTaskRepository {
    ReplenishmentTask save(ReplenishmentTask task);
    Optional<ReplenishmentTask> findById(String id);
    List<ReplenishmentTask> findAll();
    List<ReplenishmentTask> findByStatus(ReplenishmentTaskStatus status);
    List<ReplenishmentTask> findBySkuAndToLocation(String sku, String toLocation);
    void deleteAll();
}
