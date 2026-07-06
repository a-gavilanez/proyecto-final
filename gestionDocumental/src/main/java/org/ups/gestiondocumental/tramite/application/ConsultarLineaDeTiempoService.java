package org.ups.gestiondocumental.tramite.application;

import org.springframework.stereotype.Service;
import org.ups.gestiondocumental.tramite.application.port.in.ConsultarLineaDeTiempoUseCase;
import org.ups.gestiondocumental.tramite.application.port.out.EventoTramiteRepository;
import org.ups.gestiondocumental.tramite.application.port.out.TramiteAccessPort;
import org.ups.gestiondocumental.tramite.domain.EventoTramite;
import org.ups.gestiondocumental.tramite.domain.TipoEvento;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * El orden de los eventos (FR-004) es responsabilidad de
 * {@link EventoTramiteRepository#buscarPorTramiteOrdenadoDesc}; este servicio no
 * vuelve a ordenar para no duplicar esa regla (DRY).
 */
@Service
public class ConsultarLineaDeTiempoService implements ConsultarLineaDeTiempoUseCase {

    private final EventoTramiteRepository eventoTramiteRepository;
    private final TramiteAccessPort tramiteAccessPort;

    public ConsultarLineaDeTiempoService(EventoTramiteRepository eventoTramiteRepository,
                                          TramiteAccessPort tramiteAccessPort) {
        this.eventoTramiteRepository = eventoTramiteRepository;
        this.tramiteAccessPort = tramiteAccessPort;
    }

    @Override
    public List<EventoTramiteConsultado> consultar(UUID tramiteId, String usuarioSolicitanteId) {
        if (!tramiteAccessPort.estaAutorizado(tramiteId, usuarioSolicitanteId)) {
            throw new AccesoTramiteNoAutorizadoException(tramiteId);
        }

        // lee siempre el estado actual del repositorio, sin cache (FR-010)
        List<EventoTramite> eventos = eventoTramiteRepository.buscarPorTramiteOrdenadoDesc(tramiteId);
        if (eventos.isEmpty()) {
            throw new TramiteNoEncontradoException(tramiteId);
        }

        boolean tieneRespuesta = eventos.stream().anyMatch(e -> e.tipoEvento() == TipoEvento.RESPUESTA);

        List<EventoTramiteConsultado> resultado = new ArrayList<>(eventos.size());
        boolean pendienteYaMarcado = false;
        for (EventoTramite evento : eventos) {
            boolean esPendiente = !tieneRespuesta && !pendienteYaMarcado;
            if (esPendiente) {
                pendienteYaMarcado = true;
            }
            resultado.add(new EventoTramiteConsultado(evento, esPendiente));
        }
        return resultado;
    }
}
