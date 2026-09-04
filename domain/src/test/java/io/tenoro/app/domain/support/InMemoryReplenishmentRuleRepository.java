package io.tenoro.app.domain.support;

import io.tenoro.app.domain.model.ReplenishmentRule;
import io.tenoro.app.domain.port.outbound.ReplenishmentRuleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReplenishmentRuleRepository implements ReplenishmentRuleRepository {

    private final Map<String, ReplenishmentRule> rules = new ConcurrentHashMap<>();

    private String buildKey(String sku, String locationCode) {
        return sku.toUpperCase() + ":" + locationCode.toUpperCase();
    }

    @Override
    public ReplenishmentRule save(ReplenishmentRule rule) {
        rules.put(buildKey(rule.getSku(), rule.getLocationCode()), rule);
        return rule;
    }

    @Override
    public Optional<ReplenishmentRule> findBySkuAndLocationCode(String sku, String locationCode) {
        if (sku == null || locationCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rules.get(buildKey(sku, locationCode)));
    }

    @Override
    public List<ReplenishmentRule> findBySku(String sku) {
        if (sku == null) {
            return List.of();
        }
        String searchSku = sku.toUpperCase();
        return rules.values().stream()
                .filter(rule -> rule.getSku().equals(searchSku))
                .toList();
    }

    @Override
    public List<ReplenishmentRule> findByLocationCode(String locationCode) {
        if (locationCode == null) {
            return List.of();
        }
        String searchLoc = locationCode.toUpperCase();
        return rules.values().stream()
                .filter(rule -> rule.getLocationCode().equals(searchLoc))
                .toList();
    }

    @Override
    public List<ReplenishmentRule> findAll() {
        return new ArrayList<>(rules.values());
    }

    @Override
    public boolean existsBySkuAndLocationCode(String sku, String locationCode) {
        if (sku == null || locationCode == null) {
            return false;
        }
        return rules.containsKey(buildKey(sku, locationCode));
    }

    @Override
    public void deleteAll() {
        rules.clear();
    }
}
