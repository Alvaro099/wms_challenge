package io.tenoro.app.domain.port.outbound;

import io.tenoro.app.domain.model.ReplenishmentRule;

import java.util.List;
import java.util.Optional;

public interface ReplenishmentRuleRepository {
    ReplenishmentRule save(ReplenishmentRule rule);
    Optional<ReplenishmentRule> findBySkuAndLocationCode(String sku, String locationCode);
    List<ReplenishmentRule> findBySku(String sku);
    List<ReplenishmentRule> findByLocationCode(String locationCode);
    List<ReplenishmentRule> findAll();
    boolean existsBySkuAndLocationCode(String sku, String locationCode);
    void deleteAll();
}
