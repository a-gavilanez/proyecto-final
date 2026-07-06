# Quickstart: Validar la Línea de Tiempo del Trámite

Guía para comprobar de extremo a extremo que la funcionalidad US-06 cumple sus
criterios de aceptación (AC-1, AC-2, AC-3), una vez implementadas las tareas de
`tasks.md`.

## Prerrequisitos

- JDK 25 instalado (toolchain declarado en `build.gradle`).
- No se requieren servicios externos: la persistencia usa H2 en memoria.
- Dependencias añadidas al `build.gradle` según `research.md` (Cucumber-JVM, plugin
  `jacoco`, plugin `org.openapi.generator`) ya configuradas.
- El código generado a partir de `contracts/openapi.yaml` ya se generó
  (`./gradlew openApiGenerate` o equivalente configurado en el build).

## 1. Ejecutar toda la suite de pruebas (unitaria + integración + funcional/BDD)

```sh
./gradlew clean test
```

**Resultado esperado**: todas las pruebas unitarias (`domain`, `application`), de
integración (`infrastructure/persistence`, `infrastructure/web`) y los escenarios de
Cucumber (`src/test/resources/features/linea-tiempo-tramite.feature`) pasan en
verde.

## 2. Verificar el gate de cobertura JaCoCo

```sh
./gradlew jacocoTestReport jacocoTestCoverageVerification
```

**Resultado esperado**: el build falla si alguna clase nueva de este feature tiene
menos de 80% de cobertura, o si la cobertura global de las clases de este feature es
menor a 80%; el reporte HTML queda en
`build/reports/jacoco/test/html/index.html` (Constitución, Principio V).

## 3. Validar AC-1 manualmente (línea de tiempo completa)

1. Levantar la aplicación: `./gradlew bootRun`.
2. Insertar, vía fixture de datos de prueba (o el endpoint de soporte que uses en
   `tasks.md` para sembrar eventos), un trámite con al menos: un evento `CREACION`,
   un evento `DERIVACION` y un evento `DELEGACION` con sumilla.
3. Consultar:

   ```sh
   curl -s http://localhost:8080/api/tramites/{tramiteId}/historial | jq
   ```

4. **Resultado esperado**: la respuesta `eventos` contiene los tres eventos,
   ordenados del más reciente al más antiguo, cada uno con `actorResponsable` y
   `fechaHora` (AC-1).

## 4. Validar AC-2 (detalle de delegación)

En la misma respuesta del paso 3, localizar el evento con `tipoEvento: DELEGACION` y
confirmar que su `detalleDelegacion` incluye el texto completo de `sumilla` y el
`colaboradorAsignado` (AC-2). Si se prueba desde la interfaz cliente, expandir ese
evento en la UI y verificar que se muestra la misma información sin una llamada de
red adicional (ver research.md §7).

## 5. Validar AC-3 (estado pendiente destacado)

1. Repetir la consulta del paso 3 sobre un trámite **sin** evento `RESPUESTA`.
2. **Resultado esperado**: el evento más reciente de `eventos` tiene `pendiente: true`.
3. Insertar un evento `RESPUESTA` para ese mismo trámite y repetir la consulta.
4. **Resultado esperado**: ningún evento tiene `pendiente: true`; el evento
   `RESPUESTA` aparece como el más reciente de la lista.

## Referencias

- Contrato completo: [contracts/openapi.yaml](./contracts/openapi.yaml)
- Modelo de datos: [data-model.md](./data-model.md)
- Decisiones técnicas: [research.md](./research.md)
