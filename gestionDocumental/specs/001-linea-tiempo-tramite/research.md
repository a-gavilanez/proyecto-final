# Phase 0 Research: Línea de Tiempo Completa del Trámite

No quedaron marcadores `NEEDS CLARIFICATION` en la sección Technical Context del plan.
Este documento consolida las decisiones técnicas necesarias para diseñar la
funcionalidad conforme a la Constitución del proyecto.

## 1. Herramienta BDD para JVM

- **Decision**: Cucumber-JVM (`io.cucumber:cucumber-java`, `cucumber-spring`,
  `cucumber-junit-platform-engine`), ejecutado sobre el motor de JUnit 5 Platform.
- **Rationale**: Es el estándar de facto en el ecosistema JVM para escribir
  escenarios Given/When/Then legibles por negocio (Constitución, Principio II); se
  integra de forma nativa con JUnit 5 (ya usado por el proyecto) y con el contexto de
  Spring Boot para pruebas funcionales/de aceptación con la aplicación levantada.
- **Alternatives considered**: JBehave (menor mantenimiento y comunidad más pequeña);
  Spock (requiere introducir Groovy como lenguaje de pruebas, lo cual rompe la
  consistencia del stack 100% Java/JUnit5 ya establecido en `build.gradle`).

## 2. Herramienta de cobertura y verificación de umbrales

- **Decision**: Plugin `jacoco` nativo de Gradle, con la tarea
  `jacocoTestCoverageVerification` configurada con dos reglas: `element = CLASS` con
  mínimo `0.80` de `LINE`/`INSTRUCTION` covered ratio, y una regla `element = BUNDLE`
  (global) con mínimo `0.80`. Esta verificación se engancha a la tarea `check`.
- **Rationale**: Es el plugin oficial de Gradle, no requiere dependencias externas ni
  configuración adicional de repositorios, y soporta reglas de verificación por clase
  y globales tal como exige la Constitución (Principio V: >80% por clase, >=80%
  global).
- **Alternatives considered**: Herramientas externas tipo SonarQube/Codecov para
  aplicar el gate — se descartan porque no sustituyen el mandato explícito de "usar
  jacoco para generar los reportes"; siguen siendo compatibles como consumidores
  posteriores del reporte XML de JaCoCo si el equipo los adopta más adelante.

## 3. Generación de código a partir del contrato OpenAPI

- **Decision**: Plugin de Gradle `org.openapi.generator`, apuntando al archivo
  `contracts/openapi.yaml` de este feature, generando con el generador `spring`
  (`interfaceOnly=true`) la interfaz Java del controlador y los DTOs del modelo de
  respuesta. El controlador de `infrastructure/web` implementa esa interfaz generada.
- **Rationale**: Mantiene un flujo contract-first real (Constitución, Principio IV):
  la firma del endpoint y sus modelos se derivan mecánicamente del contrato, evitando
  que el contrato y el código diverjan. Usar `interfaceOnly=true` permite mantener la
  implementación real del controlador fuera del código generado, respetando
  Arquitectura Limpia (el código generado solo aporta el "puerto" HTTP, no lógica de
  negocio).
- **Alternatives considered**: Escribir los DTOs y la firma del controlador a mano —
  se descarta porque contradice el mandato explícito de usar openapi-generator;
  generar el contrato a partir del código con `springdoc-openapi` — se descarta
  porque invierte el flujo exigido (code-first en vez de contract-first).

## 4. Modelo de persistencia de eventos

- **Decision**: Una única tabla `evento_tramite` (registro de eventos de una sola
  tabla) con una columna discriminante `tipo_evento` y columnas opcionales para el
  detalle de delegación (`sumilla`, `colaborador_asignado`), en lugar de una tabla por
  tipo de evento.
- **Rationale**: Para el alcance actual (solo lectura, sin reglas de negocio de
  escritura complejas por tipo de evento) una tabla única simplifica la consulta
  cronológica a una sola sentencia ordenada y evita joins innecesarios, en línea con
  YAGNI/DRY (Constitución, Principio III).
- **Alternatives considered**: Tabla por tipo de evento con joins polimórficos
  (sobre-ingeniería para una funcionalidad de solo consulta); modelo EAV genérico
  (dificulta tipar y validar los campos de cada tipo de evento).

## 5. Orden de presentación de la línea de tiempo

- **Decision**: Orden descendente por fecha/hora (evento más reciente primero), con
  el identificador del evento como criterio de desempate estable para eventos con la
  misma marca de tiempo.
- **Rationale**: Confirma el supuesto ya documentado en `spec.md` (sección
  Assumptions): es el patrón estándar de una línea de tiempo de actividad y responde
  directamente a la necesidad de "saber dónde está el trámite ahora mismo" sin tener
  que desplazarse hasta el final de una lista larga.
- **Alternatives considered**: Orden ascendente (más antiguo primero) — descartado
  por el supuesto ya fijado en el spec.

## 6. Alcance frente a dependencias no implementadas (US-01, US-03, US-05)

- **Decision**: Modelar únicamente el dominio mínimo (`Trámite`, `EventoTramite`,
  detalle de delegación) necesario para sostener la consulta de línea de tiempo. Los
  eventos usados en pruebas de integración y funcionales se insertan directamente a
  través del puerto de repositorio (fixtures de prueba), sin construir los casos de
  uso completos de creación de trámite, derivación, delegación o carga de respuesta.
- **Rationale**: Esas historias (US-01, US-03, US-05 y la carga de respuesta) son
  dependencias declaradas de US-06 pero no están implementadas todavía en este
  repositorio. Construirlas por completo aquí violaría YAGNI y ampliaría el alcance
  más allá de lo pedido. Insertar eventos vía fixtures de prueba permite validar el
  comportamiento de consulta sin bloquear esta historia.
- **Alternatives considered**: Implementar primero US-01/US-03/US-05 completas —
  descartado por estar fuera del pedido actual y por retrasar innecesariamente esta
  historia; dejar el endpoint sin pruebas de integración reales — descartado porque
  viola el Principio II (BDD en los tres niveles) de la Constitución.

## 7. Superficie de la API expuesta

- **Decision**: Un único endpoint, `GET /tramites/{tramiteId}/historial`, que
  devuelve todos los eventos con su detalle completo, incluida la sumilla íntegra y
  el colaborador asignado en eventos de delegación.
- **Rationale**: El "expandir" descrito en AC-2 es una interacción de presentación
  (mostrar/ocultar contenido ya recibido) y no requiere una segunda llamada a la API,
  dado el volumen pequeño de eventos por trámite (Scale/Scope del plan). Evita crear
  una API adicional sin justificación (YAGNI).
- **Alternatives considered**: Endpoint de detalle por evento bajo demanda (lazy
  loading) — descartado por complejidad no justificada para el volumen esperado.

**Output**: Todas las decisiones técnicas quedaron resueltas; no quedan marcadores
`NEEDS CLARIFICATION` pendientes para pasar a la Fase 1.
