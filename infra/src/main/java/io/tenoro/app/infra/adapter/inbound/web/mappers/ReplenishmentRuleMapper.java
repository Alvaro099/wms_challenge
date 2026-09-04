package io.tenoro.app.infra.adapter.inbound.web.mappers;

import io.tenoro.app.api.dto.RuleResponse;
import io.tenoro.app.domain.model.ReplenishmentRule;

public final class ReplenishmentRuleMapper {

    private ReplenishmentRuleMapper() {
    }

    public static RuleResponse toResponse(ReplenishmentRule rule) {
        if (rule == null) {
            return null;
        }
        return new RuleResponse(rule.getSku(), rule.getLocationCode(), rule.getMin(), rule.getMax());
    }
}
