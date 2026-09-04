package io.tenoro.app.infra.adapter.inbound.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.tenoro.app.api.dto.CreateLocationRequest;
import io.tenoro.app.api.dto.ErrorResponse;
import io.tenoro.app.api.dto.LocationResponse;
import io.tenoro.app.domain.model.Location;
import io.tenoro.app.domain.port.inbound.WmsService;
import io.tenoro.app.infra.adapter.inbound.web.mappers.EnumMapper;
import io.tenoro.app.infra.adapter.inbound.web.mappers.LocationMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@Tag(name = "Locations", description = "Warehouse location management endpoints")
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private final WmsService wmsService;

    public LocationController(WmsService wmsService) {
        this.wmsService = wmsService;
    }

    @Operation(summary = "Crear ubicación", description = "Crea una nueva ubicación de depósito (PICKING o RESERVE).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ubicación creada con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ubicación ya existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        logger.info("POST /locations - Creating location code: {}, type: {}", request.code(), request.type());
        Location created = wmsService.createLocation(request.code(), EnumMapper.toDomain(request.type()));
        return ResponseEntity.status(HttpStatus.CREATED).body(LocationMapper.toResponse(created));
    }

    @Operation(summary = "Listar ubicaciones", description = "Devuelve todas las ubicaciones registradas en el depósito.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de ubicaciones recuperada",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LocationResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        logger.info("GET /locations - Listing all locations");
        List<LocationResponse> locations = wmsService.getAllLocations().stream()
                .map(LocationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(locations);
    }
}
