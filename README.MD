# Módulo de Inventario y Reposición de WMS

Módulo backend para un Sistema de Gestión de Almacenes (WMS) que administra el inventario por ubicación (picking y reserva), reglas de reposición min/max, y la generación y ejecución atómica de tareas de reposición.

La especificación del desafío está disponible en [SPECS.md](SPECS.md).

## Requisitos

- Java 21 o superior
- Gradle Wrapper (`gradlew` / `gradlew.bat`)

## Arquitectura

El proyecto sigue una arquitectura hexagonal (puertos y adaptadores) con cuatro módulos de Gradle:

| Módulo | Responsabilidad |
|--------|----------------|
| `domain/` | Lógica de negocio, modelos de dominio, puertos y excepciones de dominio (sin dependencias de framework) |
| `api/` | Contratos de API y DTOs (exportables, independientes del dominio) |
| `infra/` | Controladores REST, adaptadores de persistencia, configuración y manejo de excepciones |
| `src/` | Punto de entrada de la aplicación Spring Boot y tests de integración |

Flujo de dependencias: `infra` → `api` + `domain`; `domain` no tiene dependencias externas.

## Ejecutar la aplicación

```shell
./run.sh
```

O directamente con Gradle:

```shell
./gradlew bootRun
```

URL base: `http://localhost:8080/api/templates`

| Recurso | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/api/templates/documentation |
| OpenAPI JSON | http://localhost:8080/api/templates/openapi |
| Health check | http://localhost:8080/api/templates/actuator/health |

## Datos iniciales

En el primer arranque, `DataSeeder` carga el escenario definido en el desafío. Los reinicios posteriores omiten la carga de datos si ya existen.

**Ubicaciones:** `PICK-01`, `PICK-02` (PICKING) · `RSV-01`, `RSV-02`, `RSV-03` (RESERVE)

**Reglas de reposición:**

| SKU | Ubicación | Mín | Máx |
|-----|----------|-----|-----|
| SKU-100 | PICK-01 | 20 | 100 |
| SKU-200 | PICK-01 | 10 | 50 |
| SKU-300 | PICK-02 | 30 | 120 |

**Stock inicial:**

| SKU | Ubicación | Cantidad |
|-----|----------|----------|
| SKU-100 | PICK-01 | 5 |
| SKU-200 | PICK-01 | 40 |
| SKU-300 | PICK-02 | 10 |
| SKU-100 | RSV-01 | 60 |
| SKU-100 | RSV-02 | 50 |
| SKU-300 | RSV-03 | 70 |

## Testing

```shell
./gradlew test
```

- `WmsDomainServiceTest` — tests unitarios de las reglas de negocio del dominio (sin contexto de Spring)
- `WmsIntegrationTest` — tests end-to-end de los endpoints REST

## Referencia de la API

URL base: `http://localhost:8080/api/templates`

### Ubicaciones

```shell
# Crear ubicación
curl -X POST http://localhost:8080/api/templates/locations \
  -H "Content-Type: application/json" \
  -d '{"code":"PICK-03","type":"PICKING"}'

# Listar ubicaciones
curl http://localhost:8080/api/templates/locations
```

### Stock

```shell
# Fijar stock
curl -X POST http://localhost:8080/api/templates/stock \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-100","locationCode":"PICK-01","quantity":15}'

# Consultar stock
curl "http://localhost:8080/api/templates/stock?sku=SKU-100"
curl "http://localhost:8080/api/templates/stock?sku=SKU-100&location=PICK-01"

# Mover stock (transferencia atómica)
curl -X POST http://localhost:8080/api/templates/stock/move \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-100","from":"RSV-01","to":"PICK-01","quantity":20}'
```

### Reglas de reposición

```shell
curl -X POST http://localhost:8080/api/templates/replenishment-rules \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-400","locationCode":"PICK-02","min":15,"max":80}'
```

### Tareas de reposición

```shell
# Evaluar y generar tareas
curl -X POST http://localhost:8080/api/templates/replenishment/tasks \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-100","locationCode":"PICK-01"}'

# Listar tareas (filtro opcional por estado)
curl http://localhost:8080/api/templates/replenishment/tasks
curl "http://localhost:8080/api/templates/replenishment/tasks?status=OPEN"

# Confirmar tarea (mueve el stock y cierra la tarea)
curl -X POST http://localhost:8080/api/templates/replenishment/tasks/{taskId}/confirm

# Cancelar tarea (sin movimiento de stock)
curl -X POST http://localhost:8080/api/templates/replenishment/tasks/{taskId}/cancel

# Escanear el almacén y generar todas las tareas pendientes
curl -X POST http://localhost:8080/api/templates/replenishment/scan
```

## Manejo de errores

Todos los errores devuelven una estructura JSON uniforme:

```json
{
  "message": "Descripción del error",
  "status": 404,
  "timestamp": "2026-09-04T19:00:00Z"
}
```

Códigos de estado HTTP: `400` (validación/regla de dominio), `404` (no encontrado), `409` (conflicto), `500` (error inesperado).
