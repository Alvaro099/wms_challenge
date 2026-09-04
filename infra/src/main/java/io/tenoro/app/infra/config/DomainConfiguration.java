package io.tenoro.app.infra.config;

import io.tenoro.app.domain.port.inbound.WmsService;
import io.tenoro.app.domain.port.outbound.*;
import io.tenoro.app.domain.service.WmsDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public WmsService wmsService(LocationRepository locationRepository,
                                 InventoryRepository inventoryRepository,
                                 ReplenishmentRuleRepository replenishmentRuleRepository,
                                 ReplenishmentTaskRepository replenishmentTaskRepository) {
        return new WmsDomainService(locationRepository, inventoryRepository, replenishmentRuleRepository, replenishmentTaskRepository);
    }
}
