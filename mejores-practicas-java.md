# Checklist de Mejores Prácticas — Proyecto Java

Guía de verificación para revisar la calidad de un proyecto Java (aplica especialmente a proyectos Spring Boot con arquitectura hexagonal / multi-módulo).

---

## 1. Estructura y organización del proyecto

- [ ] Paquetes organizados por **dominio/feature**, no solo por capa técnica (evitar `controllers/`, `services/`, `repositories/` como única división si el proyecto es grande).
- [ ] Si es arquitectura hexagonal: separación clara entre **dominio**, **aplicación** (casos de uso) e **infraestructura** (adaptadores).
- [ ] El dominio no depende de frameworks (sin anotaciones de Spring, JPA, etc. dentro de las entidades de dominio puras).
- [ ] Nombres de paquetes y clases consistentes y descriptivos (sin abreviaturas crípticas).
- [ ] Un módulo/capa no debe tener dependencias cíclicas con otro.

---

## 2. Código limpio

- [ ] Clases con una única responsabilidad (SRP).
- [ ] Métodos cortos y con un solo nivel de abstracción.
- [ ] Nombres de variables y métodos que expliquen intención, sin necesidad de comentarios.
- [ ] Evitar código duplicado (DRY) — extraer a métodos/clases utilitarias o servicios de dominio.
- [ ] Evitar "magic numbers" y strings sueltos — usar constantes o enums.
- [ ] Uso de `Optional` en vez de retornar `null` cuando aplica.
- [ ] Inmutabilidad donde sea posible (campos `final`, records para DTOs/value objects).
- [ ] Uso de `records` de Java (17+) para DTOs y value objects en vez de clases con boilerplate.

---

## 3. Manejo de errores

- [ ] Excepciones específicas de dominio en vez de usar `RuntimeException` genérica.
- [ ] Manejo centralizado de errores (por ejemplo `@ControllerAdvice` en Spring) en vez de try/catch repetidos en cada controller.
- [ ] No silenciar excepciones (evitar `catch (Exception e) {}` vacío).
- [ ] Mensajes de error claros y consistentes en las respuestas de la API (formato de error uniforme).
- [ ] Validación de inputs en el borde de la aplicación (controllers/DTOs), no dispersa en el dominio.

---

## 4. Testing

- [ ] Tests unitarios para la lógica de dominio (sin levantar el contexto de Spring).
- [ ] Tests de integración para los adaptadores (repositorios, controllers).
- [ ] Cobertura razonable en la lógica de negocio crítica (no necesariamente 100%, pero sí en los casos de uso principales).
- [ ] Tests legibles: patrón **Given-When-Then** o **Arrange-Act-Assert**.
- [ ] Uso de mocks/stubs para dependencias externas (no golpear una base de datos real en tests unitarios).
- [ ] Nombres de tests descriptivos (`shouldThrowExceptionWhenStockIsInsufficient`, no `test1`).
- [ ] Evitar tests frágiles que dependan de orden de ejecución o estado compartido.

---

## 5. Dependencias e inyección

- [ ] Inyección de dependencias por **constructor**, no por campo (`@Autowired` en campos) — facilita testing e inmutabilidad.
- [ ] Interfaces para los puertos (en arquitectura hexagonal) y las implementaciones como adaptadores separados.
- [ ] Dependencias externas (versiones de librerías) actualizadas y sin vulnerabilidades conocidas.
- [ ] Evitar dependencias innecesarias o librerías no usadas en el `build.gradle` / `pom.xml`.

---

## 6. Persistencia y base de datos

- [ ] Entidades JPA separadas de los modelos de dominio (evitar "anemic domain model" filtrado por JPA).
- [ ] Migraciones de base de datos versionadas (Flyway o Liquibase) en vez de `ddl-auto: update` en producción.
- [ ] Índices adecuados en columnas usadas en búsquedas frecuentes.
- [ ] Uso de transacciones (`@Transactional`) donde corresponde, con el alcance correcto (no demasiado amplio).
- [ ] Evitar el problema N+1 en consultas (revisar fetch types, usar `@EntityGraph` o queries específicas).

---

## 7. API / Controllers

- [ ] Códigos de estado HTTP correctos (200, 201, 400, 404, 409, 422, 500, etc.) según el caso.
- [ ] DTOs de entrada/salida separados de las entidades de dominio (no exponer entidades JPA directamente).
- [ ] Validación de DTOs con `jakarta.validation` (`@NotNull`, `@Valid`, etc.).
- [ ] Documentación de la API (OpenAPI/Swagger) actualizada.
- [ ] Versionado de API si el proyecto lo requiere.

---

## 8. Configuración

- [ ] Configuración externalizada (`application.yml`/`.properties`), sin valores hardcodeados en el código.
- [ ] Perfiles separados por ambiente (`dev`, `test`, `prod`).
- [ ] Secretos y credenciales fuera del código fuente (variables de entorno, vault, etc.), nunca commiteados.
- [ ] Logging configurado con niveles apropiados (no dejar `DEBUG` en producción).

---

## 9. Concurrencia y rendimiento

- [ ] Uso correcto de pools de conexiones (HikariCP configurado con valores razonables).
- [ ] Operaciones bloqueantes evitadas en hilos reactivos, si el proyecto usa WebFlux.
- [ ] Evitar cálculos costosos repetidos sin necesidad — usar caché donde tenga sentido (`@Cacheable`).

---

## 10. Convenciones y herramientas de calidad

- [ ] Formateo de código consistente (Checkstyle, Spotless, o similar).
- [ ] Análisis estático configurado (SonarQube, SpotBugs, PMD) para detectar code smells.
- [ ] Convenciones de commits claras (Conventional Commits u otra convención del equipo).
- [ ] README actualizado con instrucciones de build, ejecución y testing.
- [ ] CI configurado para correr build + tests en cada push/PR.

---

## 11. Seguridad

- [ ] Validación y sanitización de inputs para evitar inyección (SQL injection, etc.) — uso de queries parametrizadas.
- [ ] Autenticación/autorización implementada correctamente (Spring Security) si la API lo requiere.
- [ ] No loguear información sensible (contraseñas, tokens, datos personales).
- [ ] Dependencias escaneadas por vulnerabilidades conocidas (OWASP Dependency-Check, Snyk, etc.).

---

## 12. Checklist rápido antes de entregar/mergear

1. ¿El proyecto compila y corre sin errores? (`./gradlew build`)
2. ¿Todos los tests pasan?
3. ¿El código sigue las convenciones de estilo del equipo/proyecto?
4. ¿Hay documentación mínima (README, comentarios donde sea necesario)?
5. ¿Se removieron logs de debug, TODOs olvidados y código comentado sin usar?
6. ¿Los commits son claros y atómicos?
7. ¿Se revisaron los casos borde y de error, no solo el "happy path"?

---

*Guía de referencia general de buenas prácticas para proyectos Java/Spring Boot.*
