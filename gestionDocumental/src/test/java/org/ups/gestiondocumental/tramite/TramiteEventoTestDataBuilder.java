package org.ups.gestiondocumental.tramite;

import org.ups.gestiondocumental.tramite.application.port.out.EventoTramiteRepository;
import org.ups.gestiondocumental.tramite.domain.DetalleDelegacion;
import org.ups.gestiondocumental.tramite.domain.EventoTramite;
import org.ups.gestiondocumental.tramite.domain.TipoEvento;

import java.time.Instant;
import java.util.UUID;

/**
 * Siembra trámites/eventos directamente vía {@link EventoTramiteRepository}, sin
 * pasar por US-01/US-03/US-05 (que aún no existen en este repositorio), tal como
 * documenta research.md §6.
 */
public class TramiteEventoTestDataBuilder {

    private final EventoTramiteRepository repositorio;

    public TramiteEventoTestDataBuilder(EventoTramiteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public EventoTramite sembrarCreacion(UUID tramiteId, String actorResponsable, Instant fechaHora) {
        return sembrar(new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.CREACION, actorResponsable,
                fechaHora, null, null));
    }

    public EventoTramite sembrarDerivacion(UUID tramiteId, String actorResponsable, Instant fechaHora) {
        return sembrar(new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.DERIVACION, actorResponsable,
                fechaHora, null, null));
    }

    public EventoTramite sembrarDelegacion(UUID tramiteId, String actorResponsable, Instant fechaHora,
                                            String sumilla, String colaboradorAsignado) {
        DetalleDelegacion detalle = new DetalleDelegacion(sumilla, colaboradorAsignado);
        return sembrar(new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.DELEGACION, actorResponsable,
                fechaHora, detalle, null));
    }

    public EventoTramite sembrarRespuesta(UUID tramiteId, String actorResponsable, Instant fechaHora) {
        return sembrar(new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.RESPUESTA, actorResponsable,
                fechaHora, null, null));
    }

    public EventoTramite sembrarOtro(UUID tramiteId, String actorResponsable, Instant fechaHora,
                                      String etiquetaGenerica) {
        return sembrar(new EventoTramite(UUID.randomUUID(), tramiteId, TipoEvento.OTRO, actorResponsable,
                fechaHora, null, etiquetaGenerica));
    }

    private EventoTramite sembrar(EventoTramite evento) {
        repositorio.guardar(evento);
        return evento;
    }
}
