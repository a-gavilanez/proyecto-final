package org.ups.gestiondocumental.tramite.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.ups.gestiondocumental.tramite.TramiteEventoTestDataBuilder;
import org.ups.gestiondocumental.tramite.application.EventoTramiteConsultado;
import org.ups.gestiondocumental.tramite.application.port.in.ConsultarLineaDeTiempoUseCase;
import org.ups.gestiondocumental.tramite.application.port.out.EventoTramiteRepository;
import org.ups.gestiondocumental.tramite.domain.EventoTramite;
import org.ups.gestiondocumental.tramite.domain.TipoEvento;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps Given/When/Then (en español) para linea-tiempo-tramite.feature
 * (Constitución Principio II).
 */
public class TramiteHistorialSteps {

    private static final String USUARIO_AUTORIZADO = "colaborador-1";

    @Autowired
    private ConsultarLineaDeTiempoUseCase consultarLineaDeTiempoUseCase;

    @Autowired
    private EventoTramiteRepository eventoTramiteRepository;

    private final Map<String, UUID> tramitesPorCodigo = new HashMap<>();
    private List<EventoTramiteConsultado> resultado;

    @Before
    public void reiniciarEstadoDelEscenario() {
        tramitesPorCodigo.clear();
        resultado = null;
    }

    @Dado("que el trámite {string} tiene los siguientes eventos registrados")
    public void queElTramiteTieneLosSiguientesEventosRegistrados(String tramiteCodigo, DataTable tabla) {
        UUID tramiteId = tramiteIdPara(tramiteCodigo);
        TramiteEventoTestDataBuilder datos = new TramiteEventoTestDataBuilder(eventoTramiteRepository);
        for (Map<String, String> fila : tabla.asMaps(String.class, String.class)) {
            TipoEvento tipo = TipoEvento.valueOf(fila.get("tipoEvento"));
            String actor = fila.get("actor");
            Instant fecha = Instant.parse(fila.get("fechaHora"));
            switch (tipo) {
                case CREACION -> datos.sembrarCreacion(tramiteId, actor, fecha);
                case DERIVACION -> datos.sembrarDerivacion(tramiteId, actor, fecha);
                case RESPUESTA -> datos.sembrarRespuesta(tramiteId, actor, fecha);
                default -> throw new IllegalArgumentException("Tipo de evento no soportado en este step: " + tipo);
            }
        }
    }

    @Dado("que el trámite {string} tiene una delegación con sumilla {string} y colaborador {string}")
    public void queElTramiteTieneUnaDelegacionConSumillaYColaborador(String tramiteCodigo, String sumilla,
                                                                      String colaborador) {
        UUID tramiteId = tramiteIdPara(tramiteCodigo);
        new TramiteEventoTestDataBuilder(eventoTramiteRepository)
                .sembrarDelegacion(tramiteId, "Director Ana Ruiz", Instant.parse("2026-07-02T09:00:00Z"),
                        sumilla, colaborador);
    }

    @Dado("que el trámite {string} tiene una delegación sin sumilla y colaborador {string}")
    public void queElTramiteTieneUnaDelegacionSinSumillaYColaborador(String tramiteCodigo, String colaborador) {
        UUID tramiteId = tramiteIdPara(tramiteCodigo);
        new TramiteEventoTestDataBuilder(eventoTramiteRepository)
                .sembrarDelegacion(tramiteId, "Director Ana Ruiz", Instant.parse("2026-07-02T09:00:00Z"),
                        null, colaborador);
    }

    @Cuando("un usuario autorizado consulta el historial del trámite {string}")
    public void unUsuarioAutorizadoConsultaElHistorialDelTramite(String tramiteCodigo) {
        UUID tramiteId = tramiteIdPara(tramiteCodigo);
        resultado = consultarLineaDeTiempoUseCase.consultar(tramiteId, USUARIO_AUTORIZADO);
    }

    @Entonces("la línea de tiempo devuelta contiene {int} eventos en orden del más reciente al más antiguo")
    public void laLineaDeTiempoDevueltaContieneEventosEnOrden(int cantidad) {
        assertThat(resultado).hasSize(cantidad);
    }

    @Entonces("el primer evento es de tipo {string}")
    public void elPrimerEventoEsDeTipo(String tipo) {
        assertThat(resultado.get(0).evento().tipoEvento()).isEqualTo(TipoEvento.valueOf(tipo));
    }

    @Entonces("el último evento es de tipo {string}")
    public void elUltimoEventoEsDeTipo(String tipo) {
        assertThat(resultado.get(resultado.size() - 1).evento().tipoEvento()).isEqualTo(TipoEvento.valueOf(tipo));
    }

    @Entonces("el evento de delegación incluye la sumilla completa {string}")
    public void elEventoDeDelegacionIncluyeLaSumillaCompleta(String sumilla) {
        assertThat(eventoDelegacion().detalleDelegacion().sumilla()).isEqualTo(sumilla);
    }

    @Entonces("el evento de delegación incluye el colaborador asignado {string}")
    public void elEventoDeDelegacionIncluyeElColaboradorAsignado(String colaborador) {
        assertThat(eventoDelegacion().detalleDelegacion().colaboradorAsignado()).isEqualTo(colaborador);
    }

    @Entonces("el sistema indica explícitamente que no hay sumilla registrada")
    public void elSistemaIndicaExplicitamenteQueNoHaySumillaRegistrada() {
        assertThat(eventoDelegacion().detalleDelegacion().tieneSumilla()).isFalse();
    }

    @Entonces("el evento más reciente aparece marcado como pendiente")
    public void elEventoMasRecienteApareceMarcadoComoPendiente() {
        assertThat(resultado.get(0).pendiente()).isTrue();
    }

    @Entonces("ningún evento aparece marcado como pendiente")
    public void ningunEventoApareceMarcadoComoPendiente() {
        assertThat(resultado).allMatch(consultado -> !consultado.pendiente());
    }

    private EventoTramite eventoDelegacion() {
        return resultado.stream()
                .map(EventoTramiteConsultado::evento)
                .filter(evento -> evento.tipoEvento() == TipoEvento.DELEGACION)
                .findFirst()
                .orElseThrow();
    }

    private UUID tramiteIdPara(String codigo) {
        return tramitesPorCodigo.computeIfAbsent(codigo, c -> UUID.randomUUID());
    }
}
