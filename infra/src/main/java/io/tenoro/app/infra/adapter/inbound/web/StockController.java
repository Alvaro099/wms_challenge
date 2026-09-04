package io.tenoro.app.infra.adapter.inbound.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.tenoro.app.api.dto.*;
import io.tenoro.app.domain.model.InventoryItem;
import io.tenoro.app.domain.port.inbound.WmsService;
import io.tenoro.app.infra.adapter.inbound.web.mappers.StockMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@Tag(name = "Stock", description = "Stock inventory management and movement endpoints")
public class StockController {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);
    private final WmsService wmsService;

    public StockController(WmsService wmsService) {
        this.wmsService = wmsService;
    }

    @Operation(summary = "Cargar stock", description = "Establece la cantidad disponible de un SKU en una ubicación.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad negativa o datos inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La ubicación no existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<StockResponse> setStock(@Valid @RequestBody SetStockRequest request) {
        logger.info("POST /stock - Setting stock for SKU: {} at location: {} to qty: {}",
                request.sku(), request.locationCode(), request.quantity());
        InventoryItem item = wmsService.setStock(request.sku(), request.locationCode(), request.quantity());
        return ResponseEntity.ok(StockMapper.toResponse(item));
    }

    @Operation(summary = "Consultar stock", description = "Devuelve el stock de un SKU en todas sus ubicaciones o filtrado por ubicación.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock encontrado",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = StockResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<StockResponse>> getStock(
            @Parameter(description = "SKU a consultar") @RequestParam(value = "sku", required = false) String sku,
            @Parameter(description = "Código de ubicación") @RequestParam(value = "location", required = false) String locationCode
    ) {
        logger.info("GET /stock - Querying stock for SKU: {}, location: {}", sku, locationCode);
        List<StockResponse> stockList = wmsService.getStock(sku, locationCode).stream()
                .map(StockMapper::toResponse)
                .toList();
        return ResponseEntity.ok(stockList);
    }

    @Operation(summary = "Mover stock", description = "Mueve una cantidad de un SKU de una ubicación de origen a una de destino de forma atómica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimiento realizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ubicación de origen o destino no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente en origen",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/move")
    public ResponseEntity<Void> moveStock(@Valid @RequestBody MoveStockRequest request) {
        logger.info("POST /stock/move - Moving SKU: {} qty: {} from {} to {}",
                request.sku(), request.quantity(), request.from(), request.to());
        wmsService.moveStock(request.sku(), request.from(), request.to(), request.quantity());
        return ResponseEntity.ok().build();
    }
}
