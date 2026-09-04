package io.tenoro.app.infra.adapter.inbound.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.tenoro.app.api.dto.ErrorResponse;
import io.tenoro.app.api.dto.GenerateTaskRequest;
import io.tenoro.app.api.dto.TaskResponse;
import io.tenoro.app.api.dto.TaskStatusDto;
import io.tenoro.app.domain.model.ReplenishmentTask;
import io.tenoro.app.domain.port.inbound.WmsService;
import io.tenoro.app.infra.adapter.inbound.web.mappers.EnumMapper;
import io.tenoro.app.infra.adapter.inbound.web.mappers.ReplenishmentTaskMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/replenishment")
@Tag(name = "Replenishment Tasks", description = "Replenishment task evaluation and management endpoints")
public class ReplenishmentController {

    private static final Logger logger = LoggerFactory.getLogger(ReplenishmentController.class);
    private final WmsService wmsService;

    public ReplenishmentController(WmsService wmsService) {
        this.wmsService = wmsService;
    }

    @Operation(summary = "Evaluar y generar tareas de reabasto", description = "Evalúa si un SKU en una ubicación de picking necesita reabasto y genera las tareas correspondientes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tareas de reabasto generadas (o lista vacía si no requiere)",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Regla de reabasto no configurada o ubicación inválida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ubicación no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> generateTasks(@Valid @RequestBody GenerateTaskRequest request) {
        logger.info("POST /replenishment/tasks - Evaluating replenishment for SKU: {} at location: {}",
                request.sku(), request.locationCode());
        List<ReplenishmentTask> tasks = wmsService.generateReplenishmentTasks(request.sku(), request.locationCode());
        List<TaskResponse> responses = tasks.stream().map(ReplenishmentTaskMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(summary = "Escanear y generar reabastos", description = "Escanea todo el depósito y genera todas las tareas de reabasto pendientes de una sola pasada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Escaneo completado",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class))))
    })
    @PostMapping("/scan")
    public ResponseEntity<List<TaskResponse>> scanAndGenerateAll() {
        logger.info("POST /replenishment/scan - Scanning entire warehouse for pending replenishment rules");
        List<ReplenishmentTask> tasks = wmsService.scanAndGenerateAllReplenishmentTasks();
        List<TaskResponse> responses = tasks.stream().map(ReplenishmentTaskMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Listar tareas de reabasto", description = "Obtiene la lista de tareas de reabasto (opcionalmente filtradas por estado).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tareas de reabasto recuperadas",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class))))
    })
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @Parameter(description = "Filtrar por estado (OPEN, CONFIRMED, CANCELLED)")
            @RequestParam(value = "status", required = false) TaskStatusDto status
    ) {
        logger.info("GET /replenishment/tasks - Listing tasks with status filter: {}", status);
        List<TaskResponse> responses = wmsService.getAllReplenishmentTasks(
                        status != null ? EnumMapper.toDomain(status) : null)
                .stream()
                .map(ReplenishmentTaskMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Confirmar tarea de reabasto", description = "Confirma una tarea de reabasto, mueve el stock de reserva a picking de forma atómica y cierra la tarea.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarea confirmada y stock movido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente en ubicación de reserva al intentar mover",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tasks/{id}/confirm")
    public ResponseEntity<TaskResponse> confirmTask(@PathVariable("id") String id) {
        logger.info("POST /replenishment/tasks/{}/confirm - Confirming task", id);
        ReplenishmentTask confirmed = wmsService.confirmTask(id);
        return ResponseEntity.ok(ReplenishmentTaskMapper.toResponse(confirmed));
    }

    @Operation(summary = "Cancelar tarea de reabasto", description = "Cancela una tarea de reabasto pendiente (OPEN). No mueve stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarea cancelada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tasks/{id}/cancel")
    public ResponseEntity<TaskResponse> cancelTask(@PathVariable("id") String id) {
        logger.info("POST /replenishment/tasks/{}/cancel - Cancelling task", id);
        ReplenishmentTask cancelled = wmsService.cancelTask(id);
        return ResponseEntity.ok(ReplenishmentTaskMapper.toResponse(cancelled));
    }
}
