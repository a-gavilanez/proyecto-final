package org.ups.gestiondocumental.tramite.application.port.in;

import org.ups.gestiondocumental.tramite.application.EventoTramiteConsultado;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada de US-06: consultar la línea de tiempo completa de un trámite.
 */
public interface ConsultarLineaDeTiempoUseCase {

    /**
     * @param tramiteId identificador del trámite a consultar
     * @param usuarioSolicitanteId identificador del usuario que realiza la consulta (FR-009)
     * @return eventos ordenados del más reciente al más antiguo (FR-004), con el
     *         indicador de pendiente calculado (FR-007, FR-008)
     */
    List<EventoTramiteConsultado> consultar(UUID tramiteId, String usuarioSolicitanteId);
}
