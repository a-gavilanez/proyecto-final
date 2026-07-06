# Feature Specification: Línea de Tiempo Completa del Trámite

**Feature Branch**: `001-linea-tiempo-tramite`

**Created**: 2026-07-05

**Status**: Draft

**Input**: User description: "US-06 — Consultar la línea de tiempo completa de un trámite (Épica E-03): como Colaborador o Director, quiero ver el historial cronológico de cada evento del trámite (creación, derivación, delegación, respuesta) con el actor responsable y la fecha/hora de cada acción, para entender dónde está el trámite y quién hizo qué sin tener que preguntar a nadie."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ver la línea de tiempo completa de un trámite (Priority: P1)

Como Colaborador o Director, quiero abrir un trámite y ver todos sus eventos (creación,
derivación, delegación, carga de respuesta y cualquier otro evento registrado) ordenados
cronológicamente, con el actor responsable y la fecha/hora de cada uno, para saber en qué
estado se encuentra el trámite sin tener que preguntarle a otra persona.

**Why this priority**: Es la necesidad central de la historia de usuario (US-06): sin la
línea de tiempo base no existe ninguna otra funcionalidad de auditoría del ciclo que
mostrar. Resuelve directamente el dolor de "ceguera de estado" y "sin línea de tiempo".

**Independent Test**: Puede probarse por completo abriendo un trámite que ya tiene al
menos dos eventos registrados (por ejemplo, creación y derivación) y verificando que la
sección Historial muestra ambos eventos, en orden cronológico, con actor y fecha/hora,
sin necesitar las funcionalidades de expansión de sumilla ni de destacado de pendiente.

**Acceptance Scenarios**:

1. **Given** que cualquier usuario autorizado abre un trámite, **When** accede a la
   sección Historial, **Then** ve una línea de tiempo con todos los eventos en orden
   cronológico: creación, derivación, delegación (con sumilla), carga de respuesta y
   cualquier otro evento registrado.
2. **Given** que un trámite recién creado no tiene más eventos que su creación, **When**
   el usuario abre la sección Historial, **Then** ve únicamente el evento de creación con
   su actor y fecha/hora, sin errores ni espacios vacíos que sugieran eventos faltantes.

---

### User Story 2 - Expandir el detalle de un evento de delegación (Priority: P2)

Como Colaborador o Director, quiero expandir un evento de delegación dentro de la línea
de tiempo, para leer el texto completo de la sumilla del Director, el nombre del
colaborador asignado y la fecha/hora exacta de esa delegación.

**Why this priority**: Añade valor sobre la línea de tiempo base (US1) al exponer el
contenido completo de la instrucción del Director, que es la información más sensible
para que un Colaborador entienda qué se le pidió hacer. No es indispensable para el MVP
de "ver que existió una delegación", pero sí para actuar sobre ella con contexto completo.

**Independent Test**: Puede probarse de forma independiente sobre un trámite que ya
muestra su línea de tiempo (US1) y que contiene al menos un evento de delegación:
expandir ese evento y verificar que se muestra la sumilla completa, el colaborador
asignado y la fecha/hora, sin depender de las demás historias de usuario.

**Acceptance Scenarios**:

1. **Given** que existe un evento de delegación en la línea de tiempo, **When** el
   usuario lo expande, **Then** ve el texto completo de la sumilla del Director, el
   nombre del colaborador asignado y la fecha/hora del evento.
2. **Given** que un evento de delegación no tiene sumilla registrada, **When** el usuario
   lo expande, **Then** el sistema indica claramente que no se registró sumilla, en lugar
   de mostrar un espacio vacío o un error.

---

### User Story 3 - Ver el estado pendiente destacado (Priority: P3)

Como Colaborador o Director, quiero que el estado actual del trámite se destaque
visualmente en la línea de tiempo cuando aún no se ha cargado una respuesta, para
identificar de inmediato que el trámite sigue abierto y en qué punto quedó.

**Why this priority**: Es una mejora de percepción visual sobre la línea de tiempo base
(US1): el usuario ya puede ver la ausencia de un evento de respuesta con solo leer la
lista, pero el destacado visual reduce el tiempo de interpretación. Se prioriza después
de US1 y US2 porque depende de que la línea de tiempo y sus eventos ya existan.

**Independent Test**: Puede probarse de forma independiente abriendo un trámite sin
evento de respuesta cargado y verificando que el último evento de la línea de tiempo
aparece marcado visualmente como "pendiente", sin necesitar la funcionalidad de
expansión de delegación (US2).

**Acceptance Scenarios**:

1. **Given** que el trámite aún no tiene respuesta cargada, **When** el usuario ve la
   línea de tiempo, **Then** el estado actual aparece destacado visualmente como el
   último evento pendiente.
2. **Given** que el trámite ya tiene una respuesta cargada, **When** el usuario ve la
   línea de tiempo, **Then** el destacado de "pendiente" ya no se muestra y el evento de
   respuesta aparece como el último evento completado.

---

### Edge Cases

- ¿Qué pasa cuando el trámite tiene múltiples derivaciones y delegaciones encadenadas
  (por ejemplo, delegado a un colaborador que a su vez deriva a otra área)? La línea de
  tiempo MUST mostrar cada evento por separado, en el orden en que ocurrieron.
- ¿Qué pasa si dos eventos quedaron registrados con la misma fecha/hora (por ejemplo,
  creación y primera derivación automática)? El sistema MUST usar un criterio de
  desempate estable (por ejemplo, orden de inserción/ID) para no alterar el orden en
  visualizaciones sucesivas.
- ¿Qué pasa si un usuario sin autorización sobre ese trámite intenta acceder a su
  Historial? El sistema MUST impedir el acceso y no MUST revelar ningún evento del
  trámite.
- ¿Qué pasa si un evento de delegación no tiene sumilla registrada? El sistema MUST
  indicarlo explícitamente al expandir el evento, en lugar de mostrar un campo vacío.
- ¿Qué pasa si el trámite tiene un evento cuyo tipo no es ninguno de los conocidos
  (creación, derivación, delegación, respuesta)? La línea de tiempo MUST mostrarlo igual,
  con una etiqueta genérica, actor y fecha/hora, sin romper el orden cronológico.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST mostrar, en la sección Historial de un trámite, una línea
  de tiempo con todos los eventos registrados para ese trámite.
- **FR-002**: La línea de tiempo MUST incluir, como mínimo, los siguientes tipos de
  evento cuando existan: creación, derivación, delegación, carga de respuesta, y
  cualquier otro evento registrado durante el ciclo de vida del trámite.
- **FR-003**: Cada evento de la línea de tiempo MUST mostrar el actor responsable
  (nombre y/o rol) y la fecha/hora exacta en que ocurrió la acción.
- **FR-004**: Los eventos MUST presentarse ordenados cronológicamente, del más reciente
  al más antiguo, de forma consistente cada vez que se abre la línea de tiempo.
- **FR-005**: El sistema MUST permitir expandir un evento de delegación para ver el texto
  completo de la sumilla del Director, el nombre del colaborador asignado y la
  fecha/hora del evento.
- **FR-006**: El sistema MUST indicar explícitamente cuando un evento de delegación
  expandido no tiene sumilla registrada, en lugar de mostrar un espacio vacío.
- **FR-007**: El sistema MUST destacar visualmente el estado actual como el último
  evento pendiente cuando el trámite aún no tiene una respuesta cargada.
- **FR-008**: El sistema MUST dejar de mostrar el destacado de "pendiente" en cuanto el
  trámite tenga una respuesta cargada, mostrando el evento de respuesta como el último
  evento completado.
- **FR-009**: El sistema MUST restringir el acceso a la sección Historial de un trámite
  únicamente a los usuarios autorizados sobre ese trámite (según las reglas de
  visibilidad y asignación establecidas por US-01, US-03 y US-05).
- **FR-010**: El sistema MUST reflejar en la línea de tiempo cualquier evento nuevo
  registrado sobre el trámite (derivación, delegación, respuesta u otro) sin requerir
  captura manual adicional por parte del usuario que consulta el historial.

### Key Entities *(include if feature involves data)*

- **Evento de línea de tiempo**: Representa una acción ocurrida sobre un trámite.
  Atributos clave: tipo de evento (creación, derivación, delegación, respuesta, otro),
  actor responsable, fecha/hora, y detalle asociado (por ejemplo, sumilla y colaborador
  asignado para eventos de delegación).
- **Trámite**: Entidad principal sobre la que se construye la línea de tiempo; agrupa
  todos sus eventos en orden cronológico.
- **Delegación**: Subtipo de evento con datos adicionales — sumilla del Director y
  colaborador asignado — que se muestran solo al expandir el evento.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario autorizado identifica quién realizó la última acción sobre un
  trámite y cuándo, en menos de 10 segundos desde que abre la sección Historial.
- **SC-002**: El 100% de los trámites con al menos un evento registrado muestran una
  línea de tiempo completa y en orden cronológico, sin eventos faltantes.
- **SC-003**: El 100% de los eventos de delegación pueden expandirse y muestran sumilla
  completa, colaborador asignado y fecha/hora sin errores.
- **SC-004**: El 100% de los trámites sin respuesta cargada muestran su estado actual
  destacado visualmente como evento pendiente.
- **SC-005**: Las consultas informales entre Colaboradores y Directores para saber "en
  qué va" un trámite se reducen de forma medible tras la disponibilidad de la línea de
  tiempo (objetivo de referencia: -50% frente a la línea base antes de esta
  funcionalidad).

## Assumptions

- El orden cronológico por defecto es del evento más reciente al más antiguo (patrón
  estándar de línea de tiempo/actividad), ya que la historia de usuario no especifica la
  dirección y este es el patrón más común para "saber dónde está el trámite ahora mismo".
- Las reglas de quién es un "usuario autorizado" para ver un trámite (Colaborador
  asignado, Director involucrado, u otros roles con visibilidad) ya están definidas por
  las historias de usuario de las que ésta depende (US-01, US-03, US-05); esta
  funcionalidad reutiliza esas reglas de acceso sin redefinirlas.
- Los tipos de evento distintos a creación, derivación, delegación y respuesta (mencionados
  como "cualquier otro evento registrado") se muestran con una etiqueta genérica y no
  requieren una vista de detalle expandible propia en el alcance de esta historia.
- La línea de tiempo se genera a partir de eventos que el sistema ya registra como parte
  de las acciones existentes sobre el trámite (creación, derivación, delegación,
  respuesta); esta funcionalidad no introduce una nueva forma de registrar eventos,
  solo de consultarlos.
