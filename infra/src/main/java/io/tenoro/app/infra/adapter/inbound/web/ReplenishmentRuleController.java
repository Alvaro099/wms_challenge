package io.tenoro.app.infra.adapter.inbound.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.tenoro.app.api.dto.CreateRuleRequest;
import io.tenoro.app.api.dto.ErrorResponse;
import io.tenoro.app.api.dto.RuleResponse;
import io.tenoro.app.domain.model.ReplenishmentRule;
import io.tenoro.app.domain.port.inbound.WmsService;
import io.tenoro.app.infra.adapter.inbound.web.mappers.ReplenishmentRuleMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/replenishment-rules")
@Tag(name = "Replenishment Rules", description = "Replenishment rules management endpoints")
public class ReplenishmentRuleController {

    private static final Logger logger = LoggerFactory.getLogger(ReplenishmentRuleController.class);
    private final WmsService wmsService;

    public ReplenishmentRuleController(WmsService wmsService) {
        this.wmsService = wmsService;
    }

    @Operation(summary = "Definir regla de reabasto", description = "Define el min/máx de un SKU en una ubicación de picking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Regla creada con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RuleResponse.class))),
            @ApiResponse(responseCode = "400", description = "La ubicación no es PICKING o min > max",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ubicación no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una regla para el SKU y ubicación",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody CreateRuleRequest request) {
        logger.info("POST /replenishment-rules - Creating rule for SKU: {} at location: {}, min: {}, max: {}",
                request.sku(), request.locationCode(), request.min(), request.max());
        ReplenishmentRule rule = wmsService.createReplenishmentRule(
                request.sku(), request.locationCode(), request.min(), request.max());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReplenishmentRuleMapper.toResponse(rule));
    }

    @Operation(summary = "Listar reglas de reabasto", description = "Devuelve todas las reglas de reabasto configuradas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reglas de reabasto encontradas",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RuleResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<RuleResponse>> getAllRules() {
        logger.info("GET /replenishment-rules - Listing all replenishment rules");
        List<RuleResponse> rules = wmsService.getAllReplenishmentRules().stream()
                .map(ReplenishmentRuleMapper::toResponse)
                .toList();
        return ResponseEntity.ok(rules);
    }
}
