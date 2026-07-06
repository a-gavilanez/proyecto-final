# language: es
# Escenarios BDD de US-06 (Constitucion Principio II): nivel funcional/aceptacion.
Característica: Línea de tiempo completa del trámite

  Escenario: AC-1 Ver todos los eventos en orden cronológico
    Dado que el trámite "T-100" tiene los siguientes eventos registrados
      | tipoEvento  | actor              | fechaHora            |
      | CREACION    | Mesa de partes     | 2026-07-01T09:00:00Z |
      | DERIVACION  | Director Ana Ruiz  | 2026-07-02T09:00:00Z |
    Cuando un usuario autorizado consulta el historial del trámite "T-100"
    Entonces la línea de tiempo devuelta contiene 2 eventos en orden del más reciente al más antiguo
    Y el primer evento es de tipo "DERIVACION"
    Y el último evento es de tipo "CREACION"

  Escenario: AC-2 Expandir un evento de delegación con sumilla
    Dado que el trámite "T-101" tiene una delegación con sumilla "Revisar y responder en 48 horas" y colaborador "Juan Perez"
    Cuando un usuario autorizado consulta el historial del trámite "T-101"
    Entonces el evento de delegación incluye la sumilla completa "Revisar y responder en 48 horas"
    Y el evento de delegación incluye el colaborador asignado "Juan Perez"

  Escenario: AC-2 (edge case) Delegación sin sumilla registrada
    Dado que el trámite "T-102" tiene una delegación sin sumilla y colaborador "Juan Perez"
    Cuando un usuario autorizado consulta el historial del trámite "T-102"
    Entonces el sistema indica explícitamente que no hay sumilla registrada

  Escenario: AC-3 Estado pendiente cuando no hay respuesta
    Dado que el trámite "T-103" tiene los siguientes eventos registrados
      | tipoEvento  | actor              | fechaHora            |
      | CREACION    | Mesa de partes     | 2026-07-01T09:00:00Z |
      | DERIVACION  | Director Ana Ruiz  | 2026-07-02T09:00:00Z |
    Cuando un usuario autorizado consulta el historial del trámite "T-103"
    Entonces el evento más reciente aparece marcado como pendiente

  Escenario: AC-3 (edge case) Sin estado pendiente cuando ya hay respuesta
    Dado que el trámite "T-104" tiene los siguientes eventos registrados
      | tipoEvento  | actor              | fechaHora            |
      | CREACION    | Mesa de partes     | 2026-07-01T09:00:00Z |
      | RESPUESTA   | Juan Perez         | 2026-07-03T09:00:00Z |
    Cuando un usuario autorizado consulta el historial del trámite "T-104"
    Entonces ningún evento aparece marcado como pendiente
