package org.ups.gestiondocumental.tramite.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Pruebas unitarias (BDD Given/When/Then) de los invariantes de {@link EventoTramite}
 * (data-model.md): un evento DELEGACION MUST tener detalleDelegacion; cualquier otro
 * tipo MUST NOT tenerlo; los campos obligatorios no pueden faltar.
 */
class EventoTramiteTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID TRAMITE_ID = UUID.randomUUID();
    private static final Instant FECHA = Instant.parse("2026-07-01T09:00:00Z");

    @Test
    void dadoUnEventoDelegacionSinDetalle_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> new EventoTramite(ID, TRAMITE_ID, TipoEvento.DELEGACION, "Actor", FECHA, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dadoUnEventoNoDelegacionConDetalle_cuandoSeCrea_entoncesLanzaExcepcion() {
        DetalleDelegacion detalle = new DetalleDelegacion("sumilla", "colaborador");
        assertThatThrownBy(() -> new EventoTramite(ID, TRAMITE_ID, TipoEvento.CREACION, "Actor", FECHA, detalle, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dadoUnEventoDelegacionConDetalle_cuandoSeCrea_entoncesNoLanzaExcepcion() {
        DetalleDelegacion detalle = new DetalleDelegacion("sumilla", "colaborador");
        assertThatNoException().isThrownBy(
                () -> new EventoTramite(ID, TRAMITE_ID, TipoEvento.DELEGACION, "Actor", FECHA, detalle, null));
    }

    @Test
    void dadoIdNulo_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> new EventoTramite(null, TRAMITE_ID, TipoEvento.CREACION, "Actor", FECHA, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dadoTramiteIdNulo_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> new EventoTramite(ID, null, TipoEvento.CREACION, "Actor", FECHA, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dadoTipoEventoNulo_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> new EventoTramite(ID, TRAMITE_ID, null, "Actor", FECHA, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dadoActorResponsableVacio_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> new EventoTramite(ID, TRAMITE_ID, TipoEvento.CREACION, "  ", FECHA, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dadoFechaHoraNula_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> new EventoTramite(ID, TRAMITE_ID, TipoEvento.CREACION, "Actor", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
