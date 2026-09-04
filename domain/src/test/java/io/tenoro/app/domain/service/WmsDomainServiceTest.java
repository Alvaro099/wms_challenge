package io.tenoro.app.domain.service;

import io.tenoro.app.domain.exception.*;
import io.tenoro.app.domain.model.*;
import io.tenoro.app.domain.support.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WmsDomainServiceTest {

    private InMemoryLocationRepository locationRepository;
    private InMemoryInventoryRepository inventoryRepository;
    private InMemoryReplenishmentRuleRepository ruleRepository;
    private InMemoryReplenishmentTaskRepository taskRepository;
    private WmsDomainService wmsService;

    @BeforeEach
    void setUp() {
        locationRepository = new InMemoryLocationRepository();
        inventoryRepository = new InMemoryInventoryRepository();
        ruleRepository = new InMemoryReplenishmentRuleRepository();
        taskRepository = new InMemoryReplenishmentTaskRepository();

        wmsService = new WmsDomainService(
                locationRepository,
                inventoryRepository,
                ruleRepository,
                taskRepository
        );

        // Seed basic locations
        wmsService.createLocation("PICK-01", LocationType.PICKING);
        wmsService.createLocation("PICK-02", LocationType.PICKING);
        wmsService.createLocation("RSV-01", LocationType.RESERVE);
        wmsService.createLocation("RSV-02", LocationType.RESERVE);
    }

    @Test
    @DisplayName("Debe crear ubicación exitosamente y rechazar duplicados")
    void testCreateLocation() {
        Location loc = wmsService.createLocation("PICK-99", LocationType.PICKING);
        assertEquals("PICK-99", loc.getCode());
        assertTrue(loc.isPicking());

        assertThrows(DuplicateEntityException.class, () ->
                wmsService.createLocation("PICK-99", LocationType.PICKING));
    }

    @Test
    @DisplayName("Debe mover stock atómicamente entre ubicaciones válidas")
    void testAtomicStockMoveSuccess() {
        wmsService.setStock("SKU-100", "RSV-01", 60);
        wmsService.setStock("SKU-100", "PICK-01", 5);

        wmsService.moveStock("SKU-100", "RSV-01", "PICK-01", 20);

        List<InventoryItem> rsvStock = wmsService.getStock("SKU-100", "RSV-01");
        assertEquals(40, rsvStock.get(0).getQuantity());

        List<InventoryItem> pickStock = wmsService.getStock("SKU-100", "PICK-01");
        assertEquals(25, pickStock.get(0).getQuantity());
    }

    @Test
    @DisplayName("Debe fallar al mover stock si el origen no tiene suficiente cantidad")
    void testStockMoveInsufficientStock() {
        wmsService.setStock("SKU-100", "RSV-01", 10);

        assertThrows(InsufficientStockException.class, () ->
                wmsService.moveStock("SKU-100", "RSV-01", "PICK-01", 50));
    }

    @Test
    @DisplayName("Debe rechazar creación de regla si la ubicación no es PICKING")
    void testRuleCreationFailsOnReserveLocation() {
        assertThrows(InvalidDomainRuleException.class, () ->
                wmsService.createReplenishmentRule("SKU-100", "RSV-01", 10, 50));
    }

    @Test
    @DisplayName("Debe generar tareas de reabasto cuando el stock en picking cae por debajo del mínimo")
    void testGenerateReplenishmentTasks() {
        wmsService.createReplenishmentRule("SKU-100", "PICK-01", 20, 100);
        wmsService.setStock("SKU-100", "PICK-01", 5); // Stock 5 < Min 20, deficit = 95
        wmsService.setStock("SKU-100", "RSV-01", 60);
        wmsService.setStock("SKU-100", "RSV-02", 50);

        List<ReplenishmentTask> tasks = wmsService.generateReplenishmentTasks("SKU-100", "PICK-01");

        assertEquals(2, tasks.size());
        assertEquals(60, tasks.get(0).getQuantity());
        assertEquals("RSV-01", tasks.get(0).getFromLocation());
        assertEquals("PICK-01", tasks.get(0).getToLocation());

        assertEquals(35, tasks.get(1).getQuantity());
        assertEquals("RSV-02", tasks.get(1).getFromLocation());
    }

    @Test
    @DisplayName("No debe generar tareas si el stock actual más in-flight está por encima o igual al mínimo")
    void testNoReplenishmentWhenStockAboveMin() {
        wmsService.createReplenishmentRule("SKU-100", "PICK-01", 20, 100);
        wmsService.setStock("SKU-100", "PICK-01", 25); // Stock 25 >= Min 20

        List<ReplenishmentTask> tasks = wmsService.generateReplenishmentTasks("SKU-100", "PICK-01");

        assertTrue(tasks.isEmpty());
    }

    @Test
    @DisplayName("Confirmar tarea debe cambiar estado a CONFIRMED y realizar la transferencia atómica de stock")
    void testConfirmReplenishmentTask() {
        wmsService.setStock("SKU-100", "RSV-01", 60);
        wmsService.setStock("SKU-100", "PICK-01", 5);
        wmsService.createReplenishmentRule("SKU-100", "PICK-01", 20, 100);

        List<ReplenishmentTask> tasks = wmsService.generateReplenishmentTasks("SKU-100", "PICK-01");
        ReplenishmentTask taskToConfirm = tasks.get(0); // Task for 60 from RSV-01 to PICK-01

        ReplenishmentTask confirmed = wmsService.confirmTask(taskToConfirm.getId());

        assertEquals(ReplenishmentTaskStatus.CONFIRMED, confirmed.getStatus());
        assertEquals(0, wmsService.getStock("SKU-100", "RSV-01").get(0).getQuantity());
        assertEquals(65, wmsService.getStock("SKU-100", "PICK-01").get(0).getQuantity());
    }

    @Test
    @DisplayName("Cancelar tarea debe cambiar estado a CANCELLED y NO mover stock")
    void testCancelReplenishmentTask() {
        wmsService.setStock("SKU-100", "RSV-01", 60);
        wmsService.setStock("SKU-100", "PICK-01", 5);
        wmsService.createReplenishmentRule("SKU-100", "PICK-01", 20, 100);

        List<ReplenishmentTask> tasks = wmsService.generateReplenishmentTasks("SKU-100", "PICK-01");
        ReplenishmentTask taskToCancel = tasks.get(0);

        ReplenishmentTask cancelled = wmsService.cancelTask(taskToCancel.getId());

        assertEquals(ReplenishmentTaskStatus.CANCELLED, cancelled.getStatus());
        assertEquals(60, wmsService.getStock("SKU-100", "RSV-01").get(0).getQuantity());
        assertEquals(5, wmsService.getStock("SKU-100", "PICK-01").get(0).getQuantity());
    }

    @Test
    @DisplayName("No debe permitir confirmar o cancelar una tarea que ya está en estado terminal")
    void testTerminalTaskStateTransitionsFail() {
        wmsService.setStock("SKU-100", "RSV-01", 60);
        wmsService.setStock("SKU-100", "PICK-01", 5);
        wmsService.createReplenishmentRule("SKU-100", "PICK-01", 20, 100);

        List<ReplenishmentTask> tasks = wmsService.generateReplenishmentTasks("SKU-100", "PICK-01");
        ReplenishmentTask task = tasks.get(0);

        wmsService.confirmTask(task.getId());

        assertThrows(IllegalTaskStateException.class, () -> wmsService.confirmTask(task.getId()));
        assertThrows(IllegalTaskStateException.class, () -> wmsService.cancelTask(task.getId()));
    }
}
