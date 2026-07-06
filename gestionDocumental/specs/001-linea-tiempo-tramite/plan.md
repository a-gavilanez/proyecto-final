# Implementation Plan: Línea de Tiempo Completa del Trámite

**Branch**: `001-linea-tiempo-tramite` (sin repositorio git inicializado; se trabaja directamente sobre el árbol de trabajo) | **Date**: 2026-07-05 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-linea-tiempo-tramite/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Exponer una consulta de solo lectura, `GET /tramites/{tramiteId}/historial`, que devuelve
todos los eventos de un trámite (creación, derivación, delegación, respuesta u otro) en
orden cronológico descendente (más reciente primero), con actor, fecha/hora y, para
delegaciones, la sumilla completa y el colaborador asignado. El evento más reciente se
marca como `pendiente` cuando el trámite aún no tiene una respuesta cargada. La
funcionalidad se modela como un caso de uso de solo consulta sobre un dominio mínimo
(`Trámite`, `EventoTramite`) construido con Arquitectura Limpia, contrato OpenAPI
primero, pruebas BDD en los tres niveles exigidos por la Constitución, y cobertura
verificada con JaCoCo.

## Technical Context

**Language/Version**: Java 25 (Gradle toolchain) sobre Spring Boot 4.1.0

**Primary Dependencies**: `spring-boot-starter-webmvc` (controladores REST),
`spring-boot-starter-data-jpa` (persistencia), Lombok (ya presentes en `build.gradle`).
Se añaden: Cucumber-JVM (`cucumber-java`, `cucumber-spring`,
`cucumber-junit-platform-engine`) para BDD funcional/aceptación; plugin `jacoco` de
Gradle para cobertura; plugin `org.openapi.generator` de Gradle para generar
interfaces/DTOs a partir del contrato OpenAPI.

**Storage**: H2 (ya configurado en el proyecto) vía Spring Data JPA, tanto para
desarrollo como para las pruebas de integración de esta funcionalidad.

**Testing**: JUnit 5 (unitarias, dominio y casos de uso con dobles de prueba del
repositorio); Spring Boot Test + `@DataJpaTest`/`@SpringBootTest` con H2 (integración,
persistencia y controlador REST); Cucumber-JVM con Given/When/Then sobre los
criterios de aceptación AC-1, AC-2 y AC-3 (funcional/aceptación). JaCoCo genera los
reportes de cobertura de las tres suites.

**Target Platform**: Servicio backend Spring Boot (API REST), consumido por un
cliente web fuera del alcance de este repositorio.

**Project Type**: Servicio web (backend API REST) de un único proyecto Gradle; no
existe frontend en este repositorio.

**Performance Goals**: La consulta de historial responde en menos de 500ms p95 para
trámites con hasta ~200 eventos (valor por defecto razonable; el spec no fija un
requisito numérico).

**Constraints**: Cobertura por clase >80% y cobertura global >=80% verificadas con
JaCoCo (Constitución, Principio V); el contrato OpenAPI MUST preceder la
implementación del endpoint y el código de interfaz/DTO MUST generarse con
openapi-generator (Constitución, Principio IV).

**Scale/Scope**: Un único endpoint de consulta más el modelo de dominio mínimo
(`Trámite`, `EventoTramite`, detalle de delegación) necesario para sostenerlo. No
incluye los casos de uso completos de creación de trámite, derivación, delegación ni
carga de respuesta (US-01, US-03, US-05 y la carga de respuesta correspondiente),
que se asumen como dependencias no implementadas todavía en este repositorio; para
esta funcionalidad esos eventos se insertan mediante fixtures de prueba directamente
a través del repositorio, tal como documenta `spec.md` en su sección de Assumptions.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación | Cómo lo cumple este plan |
|---|---|---|
| I. Arquitectura Limpia | PASS | Paquete `tramite` dividido en `domain` (sin dependencias externas), `application` (casos de uso + puertos, incluido `TramiteAccessPort` para autorización), `infrastructure` (adaptadores JPA, web y el adaptador simulado de autorización en `security/`). Las dependencias apuntan hacia adentro; los DTOs generados por OpenAPI y las entidades JPA viven solo en `infrastructure`. |
| II. BDD en los 3 niveles | PASS | Unitarias (JUnit5 + dobles del puerto de repositorio) sobre el caso de uso y el dominio; integración (`@DataJpaTest`, `@SpringBootTest` + MockMvc) sobre el adaptador de persistencia y el controlador; funcional/aceptación (Cucumber, Given/When/Then) sobre AC-1, AC-2, AC-3. |
| III. SOLID / YAGNI / DRY | PASS | Un solo caso de uso (`ConsultarLineaDeTiempoUseCase`) y un solo puerto de salida (`EventoTramiteRepository`); no se construyen los flujos de escritura de US-01/03/05 que no son necesarios para esta historia; el mapeo de "detalle de delegación" se centraliza en un único value object reutilizado por dominio, persistencia y respuesta HTTP. |
| IV. API First (OpenAPI) | PASS | `contracts/openapi.yaml` se escribe antes que el controlador; el plugin `org.openapi.generator` genera la interfaz del controlador y los DTOs; el código generado no se edita a mano. |
| V. Cobertura JaCoCo | PASS (gate de build) | Se añade el plugin `jacoco` con `jacocoTestCoverageVerification` exigiendo >80% por clase y >=80% global sobre las clases nuevas de este feature; el build falla si no se cumple. |

No se identifican violaciones que requieran justificación en Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-linea-tiempo-tramite/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── openapi.yaml
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/org/ups/gestiondocumental/
├── GestionDocumentalApplication.java
└── tramite/
    ├── domain/
    │   ├── Tramite.java
    │   ├── EventoTramite.java
    │   ├── TipoEvento.java              # enum: CREACION, DERIVACION, DELEGACION, RESPUESTA, OTRO
    │   └── DetalleDelegacion.java       # value object: sumilla, colaboradorAsignado
    ├── application/
    │   ├── port/
    │   │   ├── in/ConsultarLineaDeTiempoUseCase.java
    │   │   └── out/
    │   │       ├── EventoTramiteRepository.java
    │   │       └── TramiteAccessPort.java
    │   └── ConsultarLineaDeTiempoService.java
    └── infrastructure/
        ├── persistence/
        │   ├── EventoTramiteJpaEntity.java
        │   ├── EventoTramiteJpaRepository.java   # Spring Data JPA
        │   └── EventoTramiteRepositoryAdapter.java
        ├── security/
        │   └── SimulatedTramiteAccessAdapter.java   # simula FR-009 mientras US-01/03/05 no existan
        └── web/
            ├── TramiteHistorialController.java   # implementa la interfaz generada por openapi-generator
            └── EventoTramiteWebMapper.java

src/main/resources/
└── (contrato copiado/generado a partir de specs/001-linea-tiempo-tramite/contracts/openapi.yaml)

src/test/java/org/ups/gestiondocumental/tramite/
├── domain/                              # unitarias
├── application/                         # unitarias (dobles del puerto de repositorio)
├── infrastructure/persistence/          # integración (@DataJpaTest + H2)
├── infrastructure/web/                  # integración (MockMvc + @SpringBootTest)
└── bdd/
    ├── CucumberSpringConfiguration.java
    └── TramiteHistorialSteps.java

src/test/resources/features/
└── linea-tiempo-tramite.feature         # escenarios Given/When/Then para AC-1, AC-2, AC-3
```

**Structure Decision**: Proyecto único de Gradle (backend Spring Boot, sin frontend en
este repositorio). Dentro de `src/main/java` y `src/test/java` se organiza por
paquete de feature (`tramite`) con las sub-capas de Arquitectura Limpia
(`domain` → `application` → `infrastructure`), en vez de las carpetas técnicas planas
`models/services/controllers`. Las pruebas se agrupan reflejando esas mismas capas
más una carpeta `bdd/` con los steps de Cucumber; los archivos `.feature` residen en
`src/test/resources/features/` como exige la convención de Cucumber-JVM.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

Ninguna violación detectada; tabla no aplica para esta funcionalidad.
