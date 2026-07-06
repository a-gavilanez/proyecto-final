package org.ups.gestiondocumental.tramite.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.ups.gestiondocumental.tramite.application.EventoTramiteConsultado;
import org.ups.gestiondocumental.tramite.domain.DetalleDelegacion;
import org.ups.gestiondocumental.tramite.domain.EventoTramite;
import org.ups.gestiondocumental.tramite.domain.TipoEvento;
import org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.EventoLineaDeTiempo;
import org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.LineaDeTiempoResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias (BDD Given/When/Then) del mapeo de detalleDelegacion,
 * incluido el caso "sin sumilla registrada" (AC-2, FR-006).
 */
class EventoTramiteWebMapperTest {

    private final EventoTramiteWebMapper mapper = new EventoTramiteWebMapper();

    @Test
    void dadaUnaDelegacionConSumilla_cuandoSeMapea_entoncesIncluyeSumillaYColaborador() {
        // Given
        UUID tramiteId = UUID.randomUUID();
        DetalleDelegacion detalle = new DetalleDelegacion("Revisar en 48h", "Juan Perez");
        EventoTramite evento = new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.DELEGACION,
                "Directora Ana Ruiz", Instant.parse("2026-07-02T09:00:00Z"), detalle, null);

        // When
        LineaDeTiempoResponse respuesta = mapper.aRespuesta(tramiteId, List.of(new EventoTramiteConsultado(evento, false)));

        // Then
        EventoLineaDeTiempo dto = respuesta.getEventos().get(0);
        assertThat(dto.getDetalleDelegacion().getSumilla()).isEqualTo("Revisar en 48h");
        assertThat(dto.getDetalleDelegacion().getColaboradorAsignado()).isEqualTo("Juan Perez");
    }

    @Test
    void dadaUnaDelegacionSinSumilla_cuandoSeMapea_entoncesLaSumillaEsExplicitamenteNull() {
        // Given (FR-006, edge case de spec.md)
        UUID tramiteId = UUID.randomUUID();
        DetalleDelegacion detalle = new DetalleDelegacion(null, "Juan Perez");
        EventoTramite evento = new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.DELEGACION,
                "Directora Ana Ruiz", Instant.parse("2026-07-02T09:00:00Z"), detalle, null);

        // When
        LineaDeTiempoResponse respuesta = mapper.aRespuesta(tramiteId, List.of(new EventoTramiteConsultado(evento, false)));

        // Then: el campo va presente pero null, no se omite
        EventoLineaDeTiempo dto = respuesta.getEventos().get(0);
        assertThat(dto.getDetalleDelegacion().getSumilla()).isNull();
        assertThat(dto.getDetalleDelegacion().getColaboradorAsignado()).isEqualTo("Juan Perez");
    }

    @Test
    void dadoUnEventoSinDelegacion_cuandoSeMapea_entoncesDetalleDelegacionEsNulo() {
        // Given
        UUID tramiteId = UUID.randomUUID();
        EventoTramite evento = new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.CREACION,
                "Mesa de partes", Instant.parse("2026-07-01T09:00:00Z"), null, null);

        // When
        LineaDeTiempoResponse respuesta = mapper.aRespuesta(tramiteId, List.of(new EventoTramiteConsultado(evento, true)));

        // Then
        EventoLineaDeTiempo dto = respuesta.getEventos().get(0);
        assertThat(dto.getDetalleDelegacion()).isNull();
        assertThat(dto.getPendiente()).isTrue();
    }
}
