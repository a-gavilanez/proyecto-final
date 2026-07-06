package org.ups.gestiondocumental.tramite.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetalleDelegacionTest {

    @Test
    void dadoColaboradorAsignadoNulo_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> new DetalleDelegacion("sumilla", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dadoColaboradorAsignadoEnBlanco_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> new DetalleDelegacion("sumilla", "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dadaUnaSumillaConTexto_cuandoSeConsultaTieneSumilla_entoncesEsVerdadero() {
        assertThat(new DetalleDelegacion("Revisar", "Juan Perez").tieneSumilla()).isTrue();
    }

    @Test
    void dadaUnaSumillaNulaOEnBlanco_cuandoSeConsultaTieneSumilla_entoncesEsFalso() {
        assertThat(new DetalleDelegacion(null, "Juan Perez").tieneSumilla()).isFalse();
        assertThat(new DetalleDelegacion("   ", "Juan Perez").tieneSumilla()).isFalse();
    }
}
