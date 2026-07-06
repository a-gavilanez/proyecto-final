---

description: "Task list template for feature implementation"
---

# Tasks: Línea de Tiempo Completa del Trámite

**Input**: Design documents from `specs/001-linea-tiempo-tramite/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Per the project constitution (Principio II — BDD en todos los niveles), unit,
integration, and functional/acceptance tests written under a Given/When/Then BDD
approach are MANDATORY for every user story. They are included below and MUST NOT be
dropped.

**Organization**: Tasks are grouped by user story (US1, US2, US3 from spec.md) to
enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Proyecto único de Gradle (backend Spring Boot, sin frontend en este repositorio), per
`plan.md`:

- Código de producción: `src/main/java/org/ups/gestiondocumental/tramite/...`
- Pruebas: `src/test/java/org/ups/gestiondocumental/tramite/...`
- Escenarios Cucumber: `src/test/resources/features/...`
- Contrato OpenAPI: `specs/001-linea-tiempo-tramite/contracts/openapi.yaml`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar las herramientas exigidas por la Constitución (BDD, cobertura,
contract-first) antes de tocar código de dominio.

- [X] T001 Añadir las dependencias de Cucumber-JVM (`io.cucumber:cucumber-java`,
      `cucumber-spring`, `cucumber-junit-platform-engine`) en `build.gradle`
      (research.md §1)
- [X] T002 [P] Añadir y configurar el plugin `jacoco` en `build.gradle`, con
      `jacocoTestCoverageVerification` exigiendo >80% por clase (`element = CLASS`) y
      >=80% global (`element = BUNDLE`), enganchado a la tarea `check`, excluyendo el
      código generado por openapi-generator (`build/generated/openapi/**`) y la clase
      de arranque `GestionDocumentalApplication` (research.md §2, Constitución
      Principio V)
- [X] T003 [P] Añadir y configurar el plugin `org.openapi.generator` en `build.gradle`
      apuntando a `specs/001-linea-tiempo-tramite/contracts/openapi.yaml`, generador
      `spring` con `interfaceOnly=true`, generando en `build/generated/openapi`
      (research.md §3, Constitución Principio IV)

**Checkpoint**: Herramientas de BDD, cobertura y generación de API listas para usarse.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Dominio mínimo, puertos y adaptadores de persistencia compartidos por las
tres historias de usuario.

**⚠️ CRITICAL**: Ninguna historia de usuario puede empezar hasta completar esta fase.

- [X] T004 [P] Crear enum `TipoEvento` (`CREACION`, `DERIVACION`, `DELEGACION`,
      `RESPUESTA`, `OTRO`) en
      `src/main/java/org/ups/gestiondocumental/tramite/domain/TipoEvento.java`
      (data-model.md)
- [X] T005 [P] Crear value object `DetalleDelegacion` (`sumilla`,
      `colaboradorAsignado`) en
      `src/main/java/org/ups/gestiondocumental/tramite/domain/DetalleDelegacion.java`
      (data-model.md)
- [X] T006 [P] Crear entidad de dominio `Tramite` (únicamente `id`; el indicador de
      respuesta cargada es un método derivado a partir de sus eventos, no un campo
      persistido — ver data-model.md) en
      `src/main/java/org/ups/gestiondocumental/tramite/domain/Tramite.java`
- [X] T007 Crear entidad de dominio `EventoTramite` (id, tramiteId, tipoEvento,
      actorResponsable, fechaHora, detalleDelegacion, etiquetaGenerica) en
      `src/main/java/org/ups/gestiondocumental/tramite/domain/EventoTramite.java`
      (depends on T004, T005). La validación DELEGACION↔detalleDelegacion de T025 se
      incluyó aquí mismo, en el constructor compacto del record.
- [X] T008 [P] Definir el puerto de salida `EventoTramiteRepository` (buscar eventos
      por tramiteId) en
      `src/main/java/org/ups/gestiondocumental/tramite/application/port/out/EventoTramiteRepository.java`
      (depends on T007)
- [X] T009 [P] Definir el puerto de entrada `ConsultarLineaDeTiempoUseCase` en
      `src/main/java/org/ups/gestiondocumental/tramite/application/port/in/ConsultarLineaDeTiempoUseCase.java`
      (depends on T007)
- [X] T010 [P] Definir el puerto de salida `TramiteAccessPort` (verifica si el usuario
      solicitante está autorizado sobre un `tramiteId`, FR-009) en
      `src/main/java/org/ups/gestiondocumental/tramite/application/port/out/TramiteAccessPort.java`
- [X] T011 Crear `EventoTramiteJpaEntity` (tabla única `evento_tramite`, ver
      research.md §4) y `EventoTramiteJpaRepository` (Spring Data) en
      `src/main/java/org/ups/gestiondocumental/tramite/infrastructure/persistence/EventoTramiteJpaEntity.java`
      y
      `src/main/java/org/ups/gestiondocumental/tramite/infrastructure/persistence/EventoTramiteJpaRepository.java`
      (depends on T007)
- [X] T012 Implementar `EventoTramiteRepositoryAdapter` (implementa
      `EventoTramiteRepository`, mapea entidad JPA ↔ dominio) en
      `src/main/java/org/ups/gestiondocumental/tramite/infrastructure/persistence/EventoTramiteRepositoryAdapter.java`
      (depends on T008, T011)
- [X] T013 Implementar un adaptador simulado de `TramiteAccessPort` (mientras
      US-01/US-03/US-05 no estén implementadas en este repositorio, ver research.md
      §6) en
      `src/main/java/org/ups/gestiondocumental/tramite/infrastructure/security/SimulatedTramiteAccessAdapter.java`
      (depends on T010)
- [X] T014 [P] Crear `TramiteEventoTestDataBuilder` (fixtures de prueba para sembrar
      trámites/eventos directamente vía `EventoTramiteRepository`, sin pasar por
      US-01/US-03/US-05) en
      `src/test/java/org/ups/gestiondocumental/tramite/TramiteEventoTestDataBuilder.java`
      (depends on T006, T007)

**Checkpoint**: Dominio, puertos y adaptadores de persistencia listos; las historias de
usuario pueden implementarse en paralelo.

---

## Phase 3: User Story 1 - Ver la línea de tiempo completa de un trámite (Priority: P1) 🎯 MVP

**Goal**: `GET /tramites/{tramiteId}/historial` devuelve todos los eventos del trámite
ordenados del más reciente al más antiguo, con actor responsable y fecha/hora
(AC-1, FR-001 a FR-004, FR-009, FR-010).

**Independent Test**: Sembrar un trámite con eventos `CREACION` y `DERIVACION` vía
`TramiteEventoTestDataBuilder`, invocar el endpoint y verificar que ambos eventos
aparecen en orden cronológico descendente con actor y fecha/hora, sin depender de las
funcionalidades de US2 o US3.

### Tests for User Story 1 (MANDATORY - BDD Given/When/Then) ⚠️

> **NOTE: Escribir estas pruebas primero y verificar que fallan antes de implementar**

- [X] T015 [P] [US1] Unit test (Given/When/Then) de `ConsultarLineaDeTiempoService`
      verificando el orden cronológico descendente usando un doble de
      `EventoTramiteRepository` en
      `src/test/java/org/ups/gestiondocumental/tramite/application/ConsultarLineaDeTiempoServiceTest.java`
- [X] T016 [P] [US1] Integration test (`@DataJpaTest` + H2) de
      `EventoTramiteRepositoryAdapter` verificando que los eventos persistidos se
      recuperan en orden cronológico descendente en
      `src/test/java/org/ups/gestiondocumental/tramite/infrastructure/persistence/EventoTramiteRepositoryAdapterIT.java`
- [X] T017 [P] [US1] Integration test (`@SpringBootTest` + MockMvc) de
      `GET /tramites/{tramiteId}/historial` verificando la lista completa de eventos
      con actor y fecha/hora en
      `src/test/java/org/ups/gestiondocumental/tramite/infrastructure/web/TramiteHistorialControllerIT.java`
- [X] T018 [P] [US1] Escenario Cucumber Given/When/Then para AC-1 en
      `src/test/resources/features/linea-tiempo-tramite.feature`, con sus step
      definitions en
      `src/test/java/org/ups/gestiondocumental/tramite/bdd/TramiteHistorialSteps.java`
      (más `CucumberSpringConfiguration.java` y `RunCucumberTest.java` en la misma
      carpeta; este último es necesario para que Gradle descubra el motor de
      Cucumber, ver Notes de implementación al final de este archivo)
- [X] T018a [P] [US1] Integration test (`@SpringBootTest` + MockMvc) verificando que
      `GET /tramites/{tramiteId}/historial` responde 403 cuando el usuario no está
      autorizado (FR-009, edge case de spec.md) en
      `src/test/java/org/ups/gestiondocumental/tramite/infrastructure/web/TramiteHistorialControllerIT.java`
- [X] T018b [P] [US1] Unit test (Given/When/Then) verificando que un evento
      `tipoEvento = OTRO` se mapea con su `etiquetaGenerica`, actor y fecha/hora sin
      romper el orden cronológico (FR-002, edge case de spec.md) en
      `src/test/java/org/ups/gestiondocumental/tramite/application/ConsultarLineaDeTiempoServiceTest.java`

### Implementation for User Story 1

- [X] T019 [US1] Implementar `ConsultarLineaDeTiempoService` (ordena eventos desc,
      verifica autorización vía `TramiteAccessPort`, señala error si el trámite no
      existe; lee siempre el estado actual del repositorio sin caché, FR-010) en
      `src/main/java/org/ups/gestiondocumental/tramite/application/ConsultarLineaDeTiempoService.java`
      (depends on T007, T008, T009, T010)
- [X] T020 [US1] Implementar `EventoTramiteWebMapper` (dominio → DTO generado por
      openapi-generator) en
      `src/main/java/org/ups/gestiondocumental/tramite/infrastructure/web/EventoTramiteWebMapper.java`
      (depends on T003, T007). Se agregó también `TramiteExceptionHandler.java`
      (`@RestControllerAdvice`) para traducir las excepciones de aplicación a 403/404.
- [X] T021 [US1] Implementar `TramiteHistorialController` implementando la interfaz
      generada por openapi-generator, con respuestas 200/403/404 en
      `src/main/java/org/ups/gestiondocumental/tramite/infrastructure/web/TramiteHistorialController.java`
      (depends on T019, T020). Se agregó `X-Usuario-Id` como header requerido en el
      contrato OpenAPI (contract-first) para poder resolver FR-009 sin un módulo de
      autenticación real; se descartó `JsonNullable<T>` (config `openApiNullable:
      false`) porque Spring Boot 4.1 migró a Jackson 3 y `jackson-databind-nullable`
      no es compatible con su SPI de módulos.

**Checkpoint**: User Story 1 (AC-1) funcional y verificable de forma independiente.

---

## Phase 4: User Story 2 - Expandir el detalle de un evento de delegación (Priority: P2)

**Goal**: El detalle completo de un evento `DELEGACION` (sumilla completa y
colaborador asignado) viaja en la respuesta del historial, incluyendo el caso en que
no se registró sumilla (AC-2, FR-005, FR-006).

**Independent Test**: Sobre un trámite cuya línea de tiempo ya se puede consultar
(US1), sembrar un evento `DELEGACION` con sumilla y otro sin sumilla, y verificar que
la respuesta expone sumilla completa + colaborador en el primero, y una indicación
explícita de ausencia de sumilla en el segundo — sin depender de US3.

### Tests for User Story 2 (MANDATORY - BDD Given/When/Then) ⚠️

- [X] T022 [P] [US2] Unit test (Given/When/Then) de `EventoTramiteWebMapper` para el
      mapeo de `DetalleDelegacion`, incluido el caso "sin sumilla registrada" en
      `src/test/java/org/ups/gestiondocumental/tramite/infrastructure/web/EventoTramiteWebMapperTest.java`
- [X] T023 [P] [US2] Ampliar el integration test de
      `TramiteHistorialControllerIT.java` para verificar que un evento `DELEGACION`
      incluye `sumilla` completa y `colaboradorAsignado` en la respuesta JSON. Ya
      escrito como parte de T017/T018a (métodos
      `dadoUnEventoDeDelegacionConSumilla...` y `dadoUnEventoDeDelegacionSinSumilla...`).
- [X] T024 [P] [US2] Añadir el escenario Cucumber Given/When/Then para AC-2 (incluye
      el caso "sin sumilla") en
      `src/test/resources/features/linea-tiempo-tramite.feature`. Ya escrito como
      parte de T018.

### Implementation for User Story 2

- [X] T025 [US2] Añadir la validación de dominio en `EventoTramite`: `DELEGACION`
      MUST tener `DetalleDelegacion`; cualquier otro `tipoEvento` MUST NOT tenerlo
      (data-model.md) en
      `src/main/java/org/ups/gestiondocumental/tramite/domain/EventoTramite.java`.
      Se implementó en el constructor compacto del record durante T007 (Foundational),
      ya que es un invariante del propio tipo, no una capa añadida después.
- [X] T026 [US2] Añadir el manejo explícito de sumilla vacía/ausente (FR-006) en
      `src/main/java/org/ups/gestiondocumental/tramite/infrastructure/web/EventoTramiteWebMapper.java`

**Checkpoint**: User Story 1 y 2 (AC-1, AC-2) funcionando de forma independiente.

---

## Phase 5: User Story 3 - Ver el estado pendiente destacado (Priority: P3)

**Goal**: El evento más reciente se marca `pendiente = true` cuando el trámite no
tiene un evento `RESPUESTA`; en caso contrario ningún evento queda marcado como
pendiente (AC-3, FR-007, FR-008).

**Independent Test**: Sembrar un trámite sin evento `RESPUESTA` y verificar que el
evento más reciente de la respuesta tiene `pendiente: true`; sembrar otro trámite con
`RESPUESTA` y verificar que ningún evento queda marcado como pendiente — sin depender
de US2.

### Tests for User Story 3 (MANDATORY - BDD Given/When/Then) ⚠️

- [X] T027 [P] [US3] Unit test (Given/When/Then) de la regla "pendiente" en
      `ConsultarLineaDeTiempoServiceTest.java` (casos con y sin evento `RESPUESTA`)
- [X] T028 [P] [US3] Ampliar el integration test de
      `TramiteHistorialControllerIT.java` verificando `pendiente: true/false` según
      exista o no un evento `RESPUESTA`. Ya escrito como parte de T017/T018a.
- [X] T029 [P] [US3] Añadir el escenario Cucumber Given/When/Then para AC-3 en
      `src/test/resources/features/linea-tiempo-tramite.feature`. Ya escrito como
      parte de T018.

### Implementation for User Story 3

- [X] T030 [US3] Implementar el cálculo de `pendiente` (true únicamente en el evento
      más reciente cuando no existe `RESPUESTA`) en
      `src/main/java/org/ups/gestiondocumental/tramite/application/ConsultarLineaDeTiempoService.java`
      (depends on T019)
- [X] T031 [US3] Exponer el campo `pendiente` en el DTO de respuesta vía
      `src/main/java/org/ups/gestiondocumental/tramite/infrastructure/web/EventoTramiteWebMapper.java`

**Checkpoint**: Las tres historias de usuario (AC-1, AC-2, AC-3) funcionan de forma
independiente.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cerrar los requisitos transversales de la Constitución (autorización,
SOLID/YAGNI/DRY, cobertura) antes de dar la funcionalidad por completa.

- [X] T032 [P] Verificar que el test de 403 (T018a) sigue pasando tras completar US2 y
      US3, sin regresiones introducidas por el mapeo de `detalleDelegacion` o el campo
      `pendiente`. Confirmado: `./gradlew clean check` en verde con las 38 pruebas.
- [X] T033 Revisar el cumplimiento de SOLID/YAGNI/DRY sobre el paquete
      `src/main/java/org/ups/gestiondocumental/tramite/` (code review, sin archivos
      nuevos). Se detectó y eliminó `domain/Tramite.java` por no tener ningún uso real
      (violación de YAGNI); el resto del paquete cumple SRP/DIP/ISP sin hallazgos.
- [X] T034 [P] Cerrar huecos de cobertura unitaria detectados en
      `src/test/java/org/ups/gestiondocumental/tramite/**`. Se añadieron
      `EventoTramiteTest.java` y `DetalleDelegacionTest.java` para cubrir los
      invariantes de dominio no ejercitados.
- [X] T035 Ejecutar `./gradlew jacocoTestReport jacocoTestCoverageVerification` y
      confirmar >80% por clase y >=80% global (Constitución, Principio V). Resultado:
      126/126 líneas cubiertas (100%) sobre las clases del feature.
- [X] T036 Ejecutar la validación manual de
      `specs/001-linea-tiempo-tramite/quickstart.md` (AC-1, AC-2, AC-3). Verificado
      con `bootRun` + `curl` reales sobre `http://localhost:8080/api/...`: AC-1, AC-2,
      AC-3, 403 (FR-009) y 404 confirmados; datos de siembra temporales eliminados
      después de la validación.
- [X] T037 Medir el tiempo de respuesta de `GET /tramites/{tramiteId}/historial` con
      ~200 eventos sembrados y confirmar p95 <500ms (plan.md Performance Goals,
      SC-001). Cubierto por `ConsultarLineaDeTiempoPerformanceIT.java` (20 mediciones,
      p95 verificado &lt;500ms).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias — puede iniciarse de inmediato.
- **Foundational (Phase 2)**: depende de Setup — BLOQUEA las tres historias de
  usuario.
- **User Stories (Phase 3-5)**: todas dependen de Foundational; pueden avanzar en
  paralelo o en orden de prioridad P1 → P2 → P3.
- **Polish (Phase 6)**: depende de que las historias que se quieran entregar estén
  completas.

### User Story Dependencies

- **US1 (P1)**: puede iniciarse tras Foundational; no depende de US2 ni US3.
- **US2 (P2)**: puede iniciarse tras Foundational; reutiliza el endpoint de US1 pero
  es verificable de forma independiente sembrando sus propios datos.
- **US3 (P3)**: puede iniciarse tras Foundational; reutiliza el servicio de US1 pero
  es verificable de forma independiente sembrando sus propios datos.

### Within Each User Story

- Las pruebas (unitaria, integración, Cucumber) MUST escribirse y fallar antes de la
  implementación.
- Dominio/puertos (Foundational) antes que servicios de aplicación.
- Servicios de aplicación antes que el controlador REST.
- Cada historia debe quedar completa y verificada antes de pasar a la siguiente en
  entrega secuencial.

### Parallel Opportunities

- T002 y T003 (Setup) pueden ejecutarse en paralelo.
- T004, T005, T006, T008, T009, T010, T014 (Foundational, distintos archivos) pueden
  ejecutarse en paralelo.
- Una vez completada Foundational, US1, US2 y US3 pueden trabajarse en paralelo por
  distintos desarrolladores.
- Dentro de cada historia, las tareas marcadas `[P]` (pruebas de distintos tipos, en
  distintos archivos) pueden ejecutarse en paralelo.

---

## Parallel Example: User Story 1

```bash
# Lanzar juntas todas las pruebas de la User Story 1:
Task: "Unit test de ConsultarLineaDeTiempoService en application/ConsultarLineaDeTiempoServiceTest.java"
Task: "Integration test de EventoTramiteRepositoryAdapter en infrastructure/persistence/EventoTramiteRepositoryAdapterIT.java"
Task: "Integration test de TramiteHistorialController en infrastructure/web/TramiteHistorialControllerIT.java"
Task: "Escenario Cucumber AC-1 en features/linea-tiempo-tramite.feature"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup.
2. Completar Phase 2: Foundational (crítico — bloquea todas las historias).
3. Completar Phase 3: User Story 1.
4. **DETENERSE Y VALIDAR**: probar User Story 1 de forma independiente (quickstart.md
   §3).
5. Desplegar/demostrar si está listo.

### Incremental Delivery

1. Setup + Foundational → base lista.
2. Añadir US1 → probar de forma independiente → demo (MVP).
3. Añadir US2 → probar de forma independiente → demo.
4. Añadir US3 → probar de forma independiente → demo.
5. Cada historia añade valor sin romper las anteriores.

### Parallel Team Strategy

Con varios desarrolladores:

1. El equipo completa Setup + Foundational en conjunto.
2. Una vez lista Foundational:
   - Desarrollador A: User Story 1
   - Desarrollador B: User Story 2
   - Desarrollador C: User Story 3
3. Las historias se completan e integran de forma independiente.

---

## Notes

- `[P]` = archivos distintos, sin dependencias entre sí.
- La etiqueta `[US#]` asocia cada tarea a su historia de usuario para trazabilidad.
- Cada historia de usuario debe poder completarse y probarse de forma independiente.
- Verificar que las pruebas fallan antes de implementar (BDD/TDD, Constitución
  Principio II).
- Confirmar cobertura JaCoCo (>80% por clase, >=80% global) antes de cerrar la
  funcionalidad (Constitución Principio V).
- Evitar: tareas vagas, conflictos por archivo compartido entre tareas paralelas,
  dependencias cruzadas entre historias que rompan su independencia.
