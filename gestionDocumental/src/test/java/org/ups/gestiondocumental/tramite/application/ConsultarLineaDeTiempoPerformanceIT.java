package org.ups.gestiondocumental.tramite.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.ups.gestiondocumental.tramite.TramiteEventoTestDataBuilder;
import org.ups.gestiondocumental.tramite.application.port.in.ConsultarLineaDeTiempoUseCase;
import org.ups.gestiondocumental.tramite.application.port.out.EventoTramiteRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el objetivo de rendimiento del plan (plan.md: p95 &lt;500ms para
 * trámites con hasta ~200 eventos; spec.md SC-001).
 */
@SpringBootTest
class ConsultarLineaDeTiempoPerformanceIT {

    private static final String USUARIO_AUTORIZADO = "colaborador-1";
    private static final int NUMERO_DE_EVENTOS = 200;
    private static final int NUMERO_DE_MEDICIONES = 20;

    @Autowired
    private ConsultarLineaDeTiempoUseCase consultarLineaDeTiempoUseCase;

    @Autowired
    private EventoTramiteRepository eventoTramiteRepository;

    @Test
    void dadoUnTramiteConDoscientosEventos_cuandoSeConsultaRepetidamente_entoncesElP95EsMenorA500ms() {
        // Given: un tramite con 200 eventos (derivaciones alternadas con la creacion)
        UUID tramiteId = UUID.randomUUID();
        TramiteEventoTestDataBuilder datos = new TramiteEventoTestDataBuilder(eventoTramiteRepository);
        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        datos.sembrarCreacion(tramiteId, "Mesa de partes", base);
        for (int i = 1; i < NUMERO_DE_EVENTOS; i++) {
            datos.sembrarDerivacion(tramiteId, "Actor " + i, base.plusSeconds(i));
        }

        // When: se mide el tiempo de NUMERO_DE_MEDICIONES consultas (tras un warm-up)
        consultarLineaDeTiempoUseCase.consultar(tramiteId, USUARIO_AUTORIZADO);
        List<Long> duracionesEnMs = new ArrayList<>();
        for (int i = 0; i < NUMERO_DE_MEDICIONES; i++) {
            Instant inicio = Instant.now();
            List<EventoTramiteConsultado> resultado = consultarLineaDeTiempoUseCase.consultar(tramiteId, USUARIO_AUTORIZADO);
            assertThat(resultado).hasSize(NUMERO_DE_EVENTOS);
            duracionesEnMs.add(Duration.between(inicio, Instant.now()).toMillis());
        }

        // Then: el percentil 95 de las mediciones esta por debajo de 500ms
        Collections.sort(duracionesEnMs);
        int indiceP95 = (int) Math.ceil(0.95 * duracionesEnMs.size()) - 1;
        long p95 = duracionesEnMs.get(indiceP95);
        assertThat(p95).isLessThan(500L);
    }
}
