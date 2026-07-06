package org.ups.gestiondocumental.tramite.infrastructure.persistence;

import org.springframework.stereotype.Component;
import org.ups.gestiondocumental.tramite.application.port.out.EventoTramiteRepository;
import org.ups.gestiondocumental.tramite.domain.DetalleDelegacion;
import org.ups.gestiondocumental.tramite.domain.EventoTramite;
import org.ups.gestiondocumental.tramite.domain.TipoEvento;

import java.util.List;
import java.util.UUID;

@Component
public class EventoTramiteRepositoryAdapter implements EventoTramiteRepository {

    private final EventoTramiteJpaRepository jpaRepository;

    public EventoTramiteRepositoryAdapter(EventoTramiteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<EventoTramite> buscarPorTramiteOrdenadoDesc(UUID tramiteId) {
        return jpaRepository.findByTramiteIdOrderByFechaHoraDescSecuenciaDesc(tramiteId).stream()
                .map(EventoTramiteRepositoryAdapter::aDominio)
                .toList();
    }

    @Override
    public void guardar(EventoTramite evento) {
        jpaRepository.save(aEntidad(evento));
    }

    private static EventoTramite aDominio(EventoTramiteJpaEntity entidad) {
        DetalleDelegacion detalle = entidad.getTipoEvento() == TipoEvento.DELEGACION
                ? new DetalleDelegacion(entidad.getSumilla(), entidad.getColaboradorAsignado())
                : null;
        return new EventoTramite(
                entidad.getId(),
                entidad.getTramiteId(),
                entidad.getTipoEvento(),
                entidad.getActorResponsable(),
                entidad.getFechaHora(),
                detalle,
                entidad.getEtiquetaGenerica()
        );
    }

    private static EventoTramiteJpaEntity aEntidad(EventoTramite evento) {
        DetalleDelegacion detalle = evento.detalleDelegacion();
        return new EventoTramiteJpaEntity(
                evento.id(),
                evento.tramiteId(),
                evento.tipoEvento(),
                evento.actorResponsable(),
                evento.fechaHora(),
                detalle != null ? detalle.sumilla() : null,
                detalle != null ? detalle.colaboradorAsignado() : null,
                evento.etiquetaGenerica()
        );
    }
}
