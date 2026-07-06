package org.ups.gestiondocumental.tramite.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.gestiondocumental.tramite.application.port.out.EventoTramiteRepository;
import org.ups.gestiondocumental.tramite.application.port.out.TramiteAccessPort;
import org.ups.gestiondocumental.tramite.domain.EventoTramite;
import org.ups.gestiondocumental.tramite.domain.TipoEvento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias (BDD Given/When/Then) de {@link ConsultarLineaDeTiempoService},
 * usando dobles de sus puertos de salida (Constitución Principio II).
 */
@ExtendWith(MockitoExtension.class)
class ConsultarLineaDeTiempoServiceTest {

    private static final UUID TRAMITE_ID = UUID.randomUUID();
    private static final String USUARIO = "colaborador-1";

    @Mock
    private EventoTramiteRepository eventoTramiteRepository;

    @Mock
    private TramiteAccessPort tramiteAccessPort;

    private ConsultarLineaDeTiempoService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarLineaDeTiempoService(eventoTramiteRepository, tramiteAccessPort);
    }

    @Test
    void dadoEventosOrdenadosPorElRepositorio_cuandoConsulta_entoncesPreservaEseOrden() {
        // Given: el repositorio ya entrega los eventos ordenados del mas reciente al mas antiguo (FR-004)
        EventoTramite masReciente = evento(TipoEvento.DERIVACION, Instant.parse("2026-07-05T10:00:00Z"));
        EventoTramite masAntiguo = evento(TipoEvento.CREACION, Instant.parse("2026-07-01T09:00:00Z"));
        when(tramiteAccessPort.estaAutorizado(eq(TRAMITE_ID), eq(USUARIO))).thenReturn(true);
        when(eventoTramiteRepository.buscarPorTramiteOrdenadoDesc(TRAMITE_ID))
                .thenReturn(List.of(masReciente, masAntiguo));

        // When: se consulta la linea de tiempo
        List<EventoTramiteConsultado> resultado = service.consultar(TRAMITE_ID, USUARIO);

        // Then: el orden entregado por el repositorio se preserva
        assertThat(resultado).extracting(EventoTramiteConsultado::evento)
                .containsExactly(masReciente, masAntiguo);
    }

    @Test
    void dadoUsuarioNoAutorizado_cuandoConsulta_entoncesLanzaExcepcionDeAcceso() {
        // Given: el puerto de autorizacion rechaza al usuario (FR-009)
        when(tramiteAccessPort.estaAutorizado(eq(TRAMITE_ID), eq(USUARIO))).thenReturn(false);

        // When / Then: la consulta se rechaza sin llegar a leer eventos
        assertThatThrownBy(() -> service.consultar(TRAMITE_ID, USUARIO))
                .isInstanceOf(AccesoTramiteNoAutorizadoException.class);
    }

    @Test
    void dadoTramiteSinEventos_cuandoConsulta_entoncesLanzaExcepcionDeNoEncontrado() {
        // Given: el tramite esta autorizado pero no tiene ningun evento registrado
        when(tramiteAccessPort.estaAutorizado(eq(TRAMITE_ID), eq(USUARIO))).thenReturn(true);
        when(eventoTramiteRepository.buscarPorTramiteOrdenadoDesc(TRAMITE_ID)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.consultar(TRAMITE_ID, USUARIO))
                .isInstanceOf(TramiteNoEncontradoException.class);
    }

    @Test
    void dadoUnEventoDeTipoOtro_cuandoConsulta_entoncesConservaSuEtiquetaGenerica() {
        // Given: un evento de tipo OTRO con etiqueta generica (FR-002, edge case)
        EventoTramite eventoOtro = new EventoTramite(UUID.randomUUID(), TRAMITE_ID, TipoEvento.OTRO,
                "Mesa de partes", Instant.parse("2026-07-05T10:00:00Z"), null, "Reasignacion de area");
        when(tramiteAccessPort.estaAutorizado(eq(TRAMITE_ID), eq(USUARIO))).thenReturn(true);
        when(eventoTramiteRepository.buscarPorTramiteOrdenadoDesc(TRAMITE_ID)).thenReturn(List.of(eventoOtro));

        // When
        List<EventoTramiteConsultado> resultado = service.consultar(TRAMITE_ID, USUARIO);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).evento().etiquetaGenerica()).isEqualTo("Reasignacion de area");
    }

    @Test
    void dadoUnTramiteSinEventoDeRespuesta_cuandoConsulta_entoncesSoloElMasRecienteQuedaPendiente() {
        // Given (AC-3, FR-007): ningun evento es RESPUESTA
        EventoTramite masReciente = evento(TipoEvento.DERIVACION, Instant.parse("2026-07-02T09:00:00Z"));
        EventoTramite masAntiguo = evento(TipoEvento.CREACION, Instant.parse("2026-07-01T09:00:00Z"));
        when(tramiteAccessPort.estaAutorizado(eq(TRAMITE_ID), eq(USUARIO))).thenReturn(true);
        when(eventoTramiteRepository.buscarPorTramiteOrdenadoDesc(TRAMITE_ID))
                .thenReturn(List.of(masReciente, masAntiguo));

        // When
        List<EventoTramiteConsultado> resultado = service.consultar(TRAMITE_ID, USUARIO);

        // Then
        assertThat(resultado.get(0).pendiente()).isTrue();
        assertThat(resultado.get(1).pendiente()).isFalse();
    }

    @Test
    void dadoUnTramiteConEventoDeRespuesta_cuandoConsulta_entoncesNingunEventoQuedaPendiente() {
        // Given (AC-3, FR-008)
        EventoTramite respuesta = evento(TipoEvento.RESPUESTA, Instant.parse("2026-07-03T09:00:00Z"));
        EventoTramite creacion = evento(TipoEvento.CREACION, Instant.parse("2026-07-01T09:00:00Z"));
        when(tramiteAccessPort.estaAutorizado(eq(TRAMITE_ID), eq(USUARIO))).thenReturn(true);
        when(eventoTramiteRepository.buscarPorTramiteOrdenadoDesc(TRAMITE_ID))
                .thenReturn(List.of(respuesta, creacion));

        // When
        List<EventoTramiteConsultado> resultado = service.consultar(TRAMITE_ID, USUARIO);

        // Then
        assertThat(resultado).allMatch(consultado -> !consultado.pendiente());
    }

    private static EventoTramite evento(TipoEvento tipo, Instant fechaHora) {
        return new EventoTramite(UUID.randomUUID(), TRAMITE_ID, tipo, "Actor de prueba", fechaHora, null, null);
    }
}
