package org.ups.gestiondocumental.tramite.infrastructure.web;

import org.springframework.stereotype.Component;
import org.ups.gestiondocumental.tramite.application.EventoTramiteConsultado;
import org.ups.gestiondocumental.tramite.domain.DetalleDelegacion;
import org.ups.gestiondocumental.tramite.domain.EventoTramite;
import org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.EventoLineaDeTiempo;
import org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.LineaDeTiempoResponse;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Traduce entre el dominio de la línea de tiempo y los DTOs generados por
 * openapi-generator a partir del contrato (Constitución Principio IV); estos
 * detalles de formato HTTP no deben filtrarse a `domain` ni `application`.
 */
@Component
public class EventoTramiteWebMapper {

    public LineaDeTiempoResponse aRespuesta(UUID tramiteId, List<EventoTramiteConsultado> eventos) {
        LineaDeTiempoResponse respuesta = new LineaDeTiempoResponse();
        respuesta.setTramiteId(tramiteId);
        respuesta.setEventos(eventos.stream().map(this::aEventoDeRespuesta).toList());
        return respuesta;
    }

    private EventoLineaDeTiempo aEventoDeRespuesta(EventoTramiteConsultado consultado) {
        EventoTramite evento = consultado.evento();

        EventoLineaDeTiempo dto = new EventoLineaDeTiempo();
        dto.setId(evento.id());
        dto.setTipoEvento(org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.TipoEvento
                .fromValue(evento.tipoEvento().name()));
        dto.setActorResponsable(evento.actorResponsable());
        dto.setFechaHora(evento.fechaHora().atOffset(ZoneOffset.UTC));
        dto.setEtiquetaGenerica(evento.etiquetaGenerica());
        dto.setDetalleDelegacion(aDetalleDelegacion(evento.detalleDelegacion()));
        dto.setPendiente(consultado.pendiente());
        return dto;
    }

    private org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.DetalleDelegacion aDetalleDelegacion(
            DetalleDelegacion detalle) {
        if (detalle == null) {
            return null;
        }
        org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.DetalleDelegacion dto =
                new org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.DetalleDelegacion();
        // FR-006: el campo va explicitamente en null cuando no hay sumilla registrada,
        // en vez de omitirse (lo que significaria "sin informacion" para el cliente).
        dto.setSumilla(detalle.tieneSumilla() ? detalle.sumilla() : null);
        dto.setColaboradorAsignado(detalle.colaboradorAsignado());
        return dto;
    }
}
