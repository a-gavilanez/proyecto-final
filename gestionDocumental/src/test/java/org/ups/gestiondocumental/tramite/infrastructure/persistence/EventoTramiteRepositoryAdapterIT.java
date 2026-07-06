package org.ups.gestiondocumental.tramite.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.ups.gestiondocumental.tramite.domain.DetalleDelegacion;
import org.ups.gestiondocumental.tramite.domain.EventoTramite;
import org.ups.gestiondocumental.tramite.domain.TipoEvento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test (H2) del adaptador de persistencia: verifica que los eventos se
 * recuperan en orden cronologico descendente, con desempate estable (FR-004,
 * research.md §5).
 */
@DataJpaTest
@Import(EventoTramiteRepositoryAdapter.class)
class EventoTramiteRepositoryAdapterIT {

    @Autowired
    private EventoTramiteRepositoryAdapter adapter;

    @Test
    void dadoEventosPersistidosConDistintaFecha_cuandoSeBuscan_entoncesSeDevuelvenDelMasRecienteAlMasAntiguo() {
        // Given: tres eventos de un mismo tramite persistidos en orden no cronologico
        UUID tramiteId = UUID.randomUUID();
        EventoTramite creacion = evento(tramiteId, TipoEvento.CREACION, Instant.parse("2026-07-01T09:00:00Z"));
        EventoTramite derivacion = evento(tramiteId, TipoEvento.DERIVACION, Instant.parse("2026-07-02T09:00:00Z"));
        EventoTramite respuesta = evento(tramiteId, TipoEvento.RESPUESTA, Instant.parse("2026-07-05T09:00:00Z"));
        adapter.guardar(creacion);
        adapter.guardar(respuesta);
        adapter.guardar(derivacion);

        // When
        List<EventoTramite> resultado = adapter.buscarPorTramiteOrdenadoDesc(tramiteId);

        // Then
        assertThat(resultado).extracting(EventoTramite::tipoEvento)
                .containsExactly(TipoEvento.RESPUESTA, TipoEvento.DERIVACION, TipoEvento.CREACION);
    }

    @Test
    void dadoDosEventosConLaMismaFechaHora_cuandoSeBuscan_entoncesElDesempateEsEstable() {
        // Given: dos eventos con identica fecha/hora (edge case de spec.md)
        UUID tramiteId = UUID.randomUUID();
        Instant mismaFecha = Instant.parse("2026-07-01T09:00:00Z");
        EventoTramite primero = evento(tramiteId, TipoEvento.CREACION, mismaFecha);
        EventoTramite segundo = evento(tramiteId, TipoEvento.DERIVACION, mismaFecha);
        adapter.guardar(primero);
        adapter.guardar(segundo);

        // When: se consulta dos veces
        List<EventoTramite> primeraLectura = adapter.buscarPorTramiteOrdenadoDesc(tramiteId);
        List<EventoTramite> segundaLectura = adapter.buscarPorTramiteOrdenadoDesc(tramiteId);

        // Then: el orden es identico en ambas lecturas (el ultimo insertado queda primero)
        assertThat(primeraLectura).extracting(EventoTramite::tipoEvento)
                .containsExactly(TipoEvento.DERIVACION, TipoEvento.CREACION);
        assertThat(segundaLectura).extracting(EventoTramite::tipoEvento)
                .containsExactlyElementsOf(primeraLectura.stream().map(EventoTramite::tipoEvento).toList());
    }

    @Test
    void dadoUnaDelegacionPersistida_cuandoSeBusca_entoncesConservaSumillaYColaborador() {
        // Given
        UUID tramiteId = UUID.randomUUID();
        DetalleDelegacion detalle = new DetalleDelegacion("Revisar y responder en 48h", "Juan Perez");
        EventoTramite delegacion = new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.DELEGACION,
                "Directora Ana Ruiz", Instant.parse("2026-07-03T09:00:00Z"), detalle, null);
        adapter.guardar(delegacion);

        // When
        List<EventoTramite> resultado = adapter.buscarPorTramiteOrdenadoDesc(tramiteId);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).detalleDelegacion()).isEqualTo(detalle);
    }

    private static EventoTramite evento(UUID tramiteId, TipoEvento tipo, Instant fechaHora) {
        return new EventoTramite(UUID.randomUUID(), tramiteId, tipo, "Actor de prueba", fechaHora, null, null);
    }
}
