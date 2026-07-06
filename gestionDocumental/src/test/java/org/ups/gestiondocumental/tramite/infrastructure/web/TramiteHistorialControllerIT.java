package org.ups.gestiondocumental.tramite.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.ups.gestiondocumental.tramite.TramiteEventoTestDataBuilder;
import org.ups.gestiondocumental.tramite.application.port.out.EventoTramiteRepository;
import org.ups.gestiondocumental.tramite.infrastructure.security.SimulatedTramiteAccessAdapter;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test end-to-end de {@code GET /tramites/{tramiteId}/historial}
 * (AC-1, AC-2, AC-3, FR-009).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TramiteHistorialControllerIT {

    private static final String USUARIO_AUTORIZADO = "colaborador-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoTramiteRepository eventoTramiteRepository;

    private TramiteEventoTestDataBuilder datos() {
        return new TramiteEventoTestDataBuilder(eventoTramiteRepository);
    }

    @Test
    void dadoUnTramiteConEventos_cuandoSeConsultaAutorizado_entoncesDevuelveLaLineaDeTiempoCompleta() throws Exception {
        // Given (AC-1): un tramite con creacion y derivacion
        UUID tramiteId = UUID.randomUUID();
        datos().sembrarCreacion(tramiteId, "Mesa de partes", Instant.parse("2026-07-01T09:00:00Z"));
        datos().sembrarDerivacion(tramiteId, "Director Ana Ruiz", Instant.parse("2026-07-02T09:00:00Z"));

        // When / Then
        mockMvc.perform(get("/tramites/{tramiteId}/historial", tramiteId)
                        .header("X-Usuario-Id", USUARIO_AUTORIZADO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tramiteId", is(tramiteId.toString())))
                .andExpect(jsonPath("$.eventos.length()", is(2)))
                .andExpect(jsonPath("$.eventos[0].tipoEvento", is("DERIVACION")))
                .andExpect(jsonPath("$.eventos[0].actorResponsable", is("Director Ana Ruiz")))
                .andExpect(jsonPath("$.eventos[1].tipoEvento", is("CREACION")));
    }

    @Test
    void dadoUnEventoDeDelegacionConSumilla_cuandoSeConsulta_entoncesIncluyeSumillaCompletaYColaborador() throws Exception {
        // Given (AC-2)
        UUID tramiteId = UUID.randomUUID();
        datos().sembrarCreacion(tramiteId, "Mesa de partes", Instant.parse("2026-07-01T09:00:00Z"));
        datos().sembrarDelegacion(tramiteId, "Director Ana Ruiz", Instant.parse("2026-07-02T09:00:00Z"),
                "Revisar el expediente y responder en 48 horas", "Juan Perez");

        // When / Then
        mockMvc.perform(get("/tramites/{tramiteId}/historial", tramiteId)
                        .header("X-Usuario-Id", USUARIO_AUTORIZADO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos[0].tipoEvento", is("DELEGACION")))
                .andExpect(jsonPath("$.eventos[0].detalleDelegacion.sumilla",
                        is("Revisar el expediente y responder en 48 horas")))
                .andExpect(jsonPath("$.eventos[0].detalleDelegacion.colaboradorAsignado", is("Juan Perez")));
    }

    @Test
    void dadoUnEventoDeDelegacionSinSumilla_cuandoSeConsulta_entoncesLaSumillaEsExplicitamenteNull() throws Exception {
        // Given (AC-2, edge case FR-006)
        UUID tramiteId = UUID.randomUUID();
        datos().sembrarDelegacion(tramiteId, "Director Ana Ruiz", Instant.parse("2026-07-02T09:00:00Z"),
                null, "Juan Perez");

        // When / Then
        mockMvc.perform(get("/tramites/{tramiteId}/historial", tramiteId)
                        .header("X-Usuario-Id", USUARIO_AUTORIZADO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos[0].detalleDelegacion.sumilla", nullValue()))
                .andExpect(jsonPath("$.eventos[0].detalleDelegacion.colaboradorAsignado", is("Juan Perez")));
    }

    @Test
    void dadoUnTramiteSinRespuesta_cuandoSeConsulta_entoncesElUltimoEventoQuedaMarcadoPendiente() throws Exception {
        // Given (AC-3)
        UUID tramiteId = UUID.randomUUID();
        datos().sembrarCreacion(tramiteId, "Mesa de partes", Instant.parse("2026-07-01T09:00:00Z"));
        datos().sembrarDerivacion(tramiteId, "Director Ana Ruiz", Instant.parse("2026-07-02T09:00:00Z"));

        // When / Then
        mockMvc.perform(get("/tramites/{tramiteId}/historial", tramiteId)
                        .header("X-Usuario-Id", USUARIO_AUTORIZADO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos[0].pendiente", is(true)))
                .andExpect(jsonPath("$.eventos[1].pendiente", is(false)));
    }

    @Test
    void dadoUnTramiteConRespuesta_cuandoSeConsulta_entoncesNingunEventoQuedaPendiente() throws Exception {
        // Given (AC-3)
        UUID tramiteId = UUID.randomUUID();
        datos().sembrarCreacion(tramiteId, "Mesa de partes", Instant.parse("2026-07-01T09:00:00Z"));
        datos().sembrarRespuesta(tramiteId, "Juan Perez", Instant.parse("2026-07-03T09:00:00Z"));

        // When / Then
        mockMvc.perform(get("/tramites/{tramiteId}/historial", tramiteId)
                        .header("X-Usuario-Id", USUARIO_AUTORIZADO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos[0].tipoEvento", is("RESPUESTA")))
                .andExpect(jsonPath("$.eventos[0].pendiente", is(false)))
                .andExpect(jsonPath("$.eventos[1].pendiente", is(false)));
    }

    @Test
    void dadoUnUsuarioNoAutorizado_cuandoConsultaElHistorial_entoncesResponde403() throws Exception {
        // Given (FR-009, T018a): un tramite existente pero un usuario no autorizado
        UUID tramiteId = UUID.randomUUID();
        datos().sembrarCreacion(tramiteId, "Mesa de partes", Instant.parse("2026-07-01T09:00:00Z"));

        // When / Then
        mockMvc.perform(get("/tramites/{tramiteId}/historial", tramiteId)
                        .header("X-Usuario-Id", SimulatedTramiteAccessAdapter.USUARIO_NO_AUTORIZADO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo", is("TRAMITE_NO_AUTORIZADO")));
    }

    @Test
    void dadoUnTramiteInexistente_cuandoSeConsultaAutorizado_entoncesResponde404() throws Exception {
        // Given: un tramiteId sin ningun evento registrado
        UUID tramiteId = UUID.randomUUID();

        // When / Then
        mockMvc.perform(get("/tramites/{tramiteId}/historial", tramiteId)
                        .header("X-Usuario-Id", USUARIO_AUTORIZADO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("TRAMITE_NO_ENCONTRADO")));
    }
}
