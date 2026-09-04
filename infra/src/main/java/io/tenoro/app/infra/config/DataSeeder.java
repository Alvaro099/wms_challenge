package io.tenoro.app.infra.config;

import io.tenoro.app.domain.model.LocationType;
import io.tenoro.app.domain.port.inbound.WmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final WmsService wmsService;

    public DataSeeder(WmsService wmsService) {
        this.wmsService = wmsService;
    }

    @Override
    public void run(String... args) {
        if (!wmsService.getAllLocations().isEmpty()) {
            logger.info("Initial data already present, skipping seed.");
            return;
        }

        logger.info("Starting WMS initial data seeding...");

        wmsService.createLocation("PICK-01", LocationType.PICKING);
        wmsService.createLocation("PICK-02", LocationType.PICKING);
        wmsService.createLocation("RSV-01", LocationType.RESERVE);
        wmsService.createLocation("RSV-02", LocationType.RESERVE);
        wmsService.createLocation("RSV-03", LocationType.RESERVE);

        wmsService.createReplenishmentRule("SKU-100", "PICK-01", 20, 100);
        wmsService.createReplenishmentRule("SKU-200", "PICK-01", 10, 50);
        wmsService.createReplenishmentRule("SKU-300", "PICK-02", 30, 120);

        wmsService.setStock("SKU-100", "PICK-01", 5);
        wmsService.setStock("SKU-200", "PICK-01", 40);
        wmsService.setStock("SKU-300", "PICK-02", 10);
        wmsService.setStock("SKU-100", "RSV-01", 60);
        wmsService.setStock("SKU-100", "RSV-02", 50);
        wmsService.setStock("SKU-300", "RSV-03", 70);

        logger.info("WMS initial data seeding completed.");
    }
}
