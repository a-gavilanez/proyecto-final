# Phase 1 Data Model: Línea de Tiempo Completa del Trámite

## Entidades de dominio

### Trámite

Entidad principal sobre la que se construye la línea de tiempo. Para esta
funcionalidad se modela con el mínimo de campos necesarios para identificarlo y para
que la consulta de historial pueda resolver el estado "pendiente" (FR-007, FR-008).

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | identificador único | Requerido, inmutable. |
| `tieneRespuestaCargada` | booleano (derivado) | `true` si existe al menos un `EventoTramite` de tipo `RESPUESTA`; se calcula a partir de los eventos, no se persiste como bandera independiente, para evitar duplicar la fuente de verdad (DRY). |

### EventoTramite

Representa una acción ocurrida sobre un trámite. Es el elemento central de la línea
de tiempo (FR-001, FR-002, FR-003).

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | identificador único | Requerido, inmutable; se usa como criterio de desempate cuando dos eventos comparten `fechaHora` (ver research.md §5). |
| `tramiteId` | referencia a Trámite | Requerido. |
| `tipoEvento` | `TipoEvento` (enum) | Requerido. Valores: `CREACION`, `DERIVACION`, `DELEGACION`, `RESPUESTA`, `OTRO`. |
| `actorResponsable` | texto (nombre y/o rol) | Requerido (FR-003); no puede estar vacío. |
| `fechaHora` | fecha/hora | Requerido; usada para el orden cronológico descendente (FR-004). |
| `detalleDelegacion` | `DetalleDelegacion` (opcional) | Presente únicamente cuando `tipoEvento = DELEGACION`; ausente en cualquier otro tipo. |
| `etiquetaGenerica` | texto (opcional) | Se usa solo cuando `tipoEvento = OTRO`, para mostrar una etiqueta legible sin necesidad de una vista de detalle propia (ver Assumptions en spec.md). |

**Regla de validación clave**: un `EventoTramite` con `tipoEvento = DELEGACION` MUST
tener un `detalleDelegacion` asociado (aunque su campo `sumilla` pueda estar vacío,
ver FR-006); un `EventoTramite` con cualquier otro `tipoEvento` MUST NOT tener
`detalleDelegacion`.

### DetalleDelegacion (value object)

Datos adicionales que solo se muestran al expandir un evento de tipo `DELEGACION`
(FR-005, FR-006).

| Campo | Tipo | Reglas |
|---|---|---|
| `sumilla` | texto (opcional) | Si está vacío o ausente, el sistema MUST indicarlo explícitamente al expandir (FR-006) en vez de mostrar un campo vacío. |
| `colaboradorAsignado` | texto (nombre) | Requerido cuando existe `detalleDelegacion`. |

## Relaciones

```text
Trámite (1) ──── (N) EventoTramite
EventoTramite (1) ──── (0..1) DetalleDelegacion   [solo si tipoEvento = DELEGACION]
```

## Reglas derivadas para la consulta (caso de uso)

- **Orden de presentación**: los `EventoTramite` de un `tramiteId` se devuelven
  ordenados por `fechaHora` descendente; en caso de empate, por `id` descendente
  (criterio de desempate estable, ver research.md §5).
- **Estado pendiente**: si ninguno de los eventos del trámite tiene
  `tipoEvento = RESPUESTA`, el evento con `fechaHora` más reciente se marca como
  `pendiente = true` en la respuesta de la consulta (FR-007). En caso contrario,
  ningún evento se marca como pendiente y el evento de `RESPUESTA` más reciente se
  presenta como el último evento completado (FR-008). Este campo `pendiente` es
  calculado por el caso de uso, no un atributo persistido de `EventoTramite`.
- **Autorización**: la consulta MUST verificar, antes de devolver cualquier evento,
  que el usuario solicitante está autorizado sobre el `tramiteId` indicado (FR-009),
  reutilizando las reglas de acceso ya definidas por US-01/US-03/US-05 (ver
  Assumptions en spec.md). Esta funcionalidad no redefine esas reglas; el caso de uso
  las invoca a través de un puerto de autorización que se asume ya existente o se
  simula con un doble de prueba mientras esas historias no estén implementadas.

## Notas de implementación (persistencia)

Siguiendo la decisión de research.md §4, `EventoTramite` y `DetalleDelegacion` se
persisten en una única tabla `evento_tramite` (columnas `sumilla` y
`colaborador_asignado` nulas cuando `tipo_evento <> DELEGACION`). Esta tabla es un
detalle de la capa `infrastructure/persistence` y no MUST filtrarse al dominio ni a
la capa `application` (Constitución, Principio I): el dominio trabaja únicamente con
los tipos `Tramite`, `EventoTramite`, `TipoEvento` y `DetalleDelegacion` descritos
arriba.
