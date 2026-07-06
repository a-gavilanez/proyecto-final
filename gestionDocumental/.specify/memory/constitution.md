<!--
Sync Impact Report
==================
Version change: [UNSET/TEMPLATE] → 1.0.0 (initial ratification)
Modified principles: N/A (first concrete version, all principles newly defined)
Added sections:
  - Core Principles: I. Arquitectura Limpia, II. Desarrollo Guiado por Comportamiento (BDD)
    en todos los niveles de prueba, III. Buenas Prácticas de Diseño (SOLID, YAGNI, DRY),
    IV. Contrato Primero (API First) con OpenAPI, V. Cobertura de Pruebas Verificable (JaCoCo)
  - Restricciones Tecnológicas y de Herramientas
  - Flujo de Trabajo de Calidad
  - Governance
Removed sections: none (template placeholders replaced with concrete content)
Templates requiring updates:
  - .specify/templates/plan-template.md: ✅ compatible (Constitution Check section is generic
    and pulls gates dynamically; no hardcoded contradiction with new principles)
  - .specify/templates/spec-template.md: ✅ compatible (generic feature spec; API contracts
    and BDD scenarios are produced downstream in plan/tasks phases)
  - .specify/templates/tasks-template.md: ✅ updated (test sections changed from OPTIONAL to
    MANDATORY/BDD Given-When-Then per Principle II; added JaCoCo coverage verification task
    per Principle V)
  - .specify/templates/checklist-template.md: ✅ compatible (generic checklist generator, no
    hardcoded principle references)
Follow-up TODOs: none
-->

# gestionDocumental Constitution

## Core Principles

### I. Arquitectura Limpia (Clean Architecture)

El sistema MUST organizarse en las capas concéntricas definidas por Robert C. Martin:
Entidades (dominio), Casos de Uso (aplicación), Adaptadores de Interfaz (controladores,
presentadores, gateways) y Frameworks & Drivers (infraestructura: Spring, JPA, base de
datos, mensajería, UI). Las dependencias del código fuente MUST apuntar siempre hacia el
interior (Dependency Rule): el dominio no MUST conocer casos de uso, adaptadores ni
frameworks; los casos de uso no MUST conocer detalles de infraestructura. La comunicación
entre una capa interna y una externa MUST realizarse a través de interfaces (puertos)
declaradas en la capa interna e implementadas en la capa externa correspondiente. Los
detalles de framework (anotaciones de Spring, entidades JPA, drivers de base de datos,
formatos de transporte HTTP) MUST permanecer confinados a la capa de infraestructura/
adaptadores y MUST NOT filtrarse al dominio ni a los casos de uso.

**Rationale**: Aislar el núcleo de negocio de frameworks, UI y bases de datos permite
probarlo sin dependencias externas y evolucionar la tecnología (cambiar de framework,
motor de base de datos, o protocolo de transporte) sin reescribir las reglas de negocio.

### II. Desarrollo Guiado por Comportamiento (BDD) en Todos los Niveles

Toda funcionalidad MUST contar con pruebas unitarias, de integración y funcionales/de
aceptación, redactadas bajo el enfoque BDD (Given/When/Then). Las pruebas funcionales/de
aceptación MUST expresarse como escenarios legibles por el negocio antes de o junto con la
implementación. Las pruebas unitarias MUST validar entidades de dominio y casos de uso en
aislamiento, sin arrancar el framework ni la base de datos. Las pruebas de integración MUST
validar la interacción real entre adaptadores (persistencia, controladores REST,
mensajería) y las capas internas. Ninguna funcionalidad se considera completa sin sus tres
niveles de prueba correspondientes escritos y en verde.

**Rationale**: BDD alinea el comportamiento verificado del sistema con el lenguaje del
negocio y reduce la ambigüedad de requisitos; exigir los tres niveles de prueba asegura
corrección desde la unidad más pequeña hasta el flujo completo end-to-end.

### III. Buenas Prácticas de Diseño: SOLID, YAGNI, DRY

Todo diseño de clases y módulos MUST cumplir los principios SOLID (responsabilidad única,
abierto/cerrado, sustitución de Liskov, segregación de interfaces, inversión de
dependencias). El código MUST aplicar YAGNI: MUST NOT implementarse funcionalidad,
abstracción o configuración especulativa que no responda a un caso de uso actual y
verificado. El código MUST aplicar DRY: la lógica duplicada MUST extraerse a componentes
reutilizables (servicios de dominio, utilidades, casos de uso compartidos) tan pronto se
detecte duplicación significativa. Toda revisión de código MUST verificar el cumplimiento
de estos tres principios antes de aprobar un cambio.

**Rationale**: Estas prácticas previenen acoplamiento excesivo, sobre-ingeniería y deuda
técnica, manteniendo el código mantenible y extensible a largo plazo sin sacrificar
velocidad de entrega.

### IV. Contrato Primero (API First) con OpenAPI

Toda API MUST diseñarse "contract-first": el contrato OpenAPI (YAML/JSON) MUST crearse y
revisarse antes de escribir la implementación del endpoint correspondiente. El código de
controladores, DTOs e interfaces de los adaptadores REST MUST generarse o validarse
mediante openapi-generator a partir del contrato OpenAPI vigente en el repositorio.
Cualquier cambio de comportamiento de la API MUST reflejarse primero en el archivo OpenAPI
y luego regenerar el código correspondiente; el código generado MUST NOT editarse
manualmente. Los contratos OpenAPI MUST versionarse junto con el código fuente y MUST
mantenerse sincronizados con el comportamiento real de la API mediante pruebas de
contrato.

**Rationale**: API First permite que consumidores y equipos trabajen en paralelo sobre un
contrato estable, reduce discrepancias entre documentación e implementación, y
openapi-generator garantiza consistencia mecánica entre el contrato y el código.

### V. Cobertura de Pruebas Verificable (JaCoCo)

JaCoCo MUST ser la herramienta obligatoria para generar los reportes de cobertura de
pruebas del proyecto. La cobertura de pruebas por clase MUST ser superior al 80% (>80%).
La cobertura global del proyecto MUST ser mayor o igual al 80% (>=80%). El build MUST
fallar (quality gate) si alguna de estas métricas no se cumple antes de integrar cambios a
la rama principal. Los reportes de JaCoCo MUST generarse en cada ejecución del pipeline de
integración continua y MUST quedar disponibles para revisión.

**Rationale**: Umbrales de cobertura objetivos y verificables por herramienta evitan
regresiones no detectadas y refuerzan de forma medible la disciplina de pruebas exigida
por el Principio II.

## Restricciones Tecnológicas y de Herramientas

- **Lenguaje/Plataforma**: Java (toolchain 25) sobre Spring Boot; Gradle como herramienta
  de build y orquestación de tareas de calidad.
- **Persistencia**: Spring Data JPA; H2 permitido para desarrollo y pruebas. El motor
  relacional de producción, si difiere, MUST documentarse en el plan técnico de la
  funcionalidad correspondiente.
- **Pruebas**: JUnit 5 como motor de ejecución. Una herramienta de BDD (p. ej.
  Cucumber-JVM u otra equivalente) MUST integrarse para expresar los escenarios
  Given/When/Then de los niveles de integración y funcional exigidos por el Principio II.
- **Cobertura**: el plugin de JaCoCo MUST estar configurado en `build.gradle` y MUST
  ejecutarse como parte de las tareas `check`/`build`, aplicando los umbrales del
  Principio V.
- **Contratos de API**: la especificación OpenAPI 3.x MUST residir en el repositorio (p.
  ej. bajo `contracts/` o `api/openapi.yaml`) y openapi-generator MUST integrarse en el
  proceso de build para generar interfaces, DTOs y/o stubs de cliente/servidor.

## Flujo de Trabajo de Calidad

- Toda Pull Request MUST verificar cumplimiento de: separación de capas y dirección de
  dependencias (Principio I), existencia de pruebas BDD en los tres niveles (Principio
  II), principios SOLID/YAGNI/DRY (Principio III), sincronización del contrato OpenAPI con
  la implementación (Principio IV), y cumplimiento de los umbrales de cobertura de JaCoCo
  (Principio V).
- Ninguna Pull Request MUST fusionarse a la rama principal si el pipeline de CI reporta
  cobertura por debajo de los umbrales definidos en el Principio V, o si el contrato
  OpenAPI no está sincronizado con el código generado.
- Cambios de contrato de API MUST revisarse y aprobarse antes de iniciar la implementación
  del endpoint correspondiente.
- Cualquier excepción a estos principios MUST documentarse explícitamente y justificarse
  en la sección "Complexity Tracking" del plan de la funcionalidad correspondiente.

## Governance

Esta Constitución prevalece sobre cualquier otra práctica, plantilla o convención en
conflicto dentro del proyecto. Toda enmienda MUST proponerse mediante Pull Request sobre
este archivo, MUST describir su impacto en las plantillas dependientes
(`plan-template.md`, `spec-template.md`, `tasks-template.md`, `checklist-template.md`) y
MUST ser aprobada antes de fusionarse. El versionado de esta Constitución MUST seguir
semántica de versiones (MAJOR.MINOR.PATCH): MAJOR para eliminación o redefinición
incompatible de principios existentes; MINOR para adición de nuevos principios/secciones o
expansión material de guías existentes; PATCH para aclaraciones y correcciones de
redacción sin cambio de fondo. Toda revisión de código y Pull Request MUST verificar
cumplimiento de esta Constitución; la complejidad no justificada MUST rechazarse. Usar
`CLAUDE.md` y los documentos del plan de la funcionalidad vigente como guía de desarrollo
en tiempo de ejecución, siempre subordinados a los principios aquí establecidos.

**Version**: 1.0.0 | **Ratified**: 2026-07-05 | **Last Amended**: 2026-07-05
