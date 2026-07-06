package org.ups.gestiondocumental.tramite.infrastructure.security;

import org.springframework.stereotype.Component;
import org.ups.gestiondocumental.tramite.application.port.out.TramiteAccessPort;

import java.util.UUID;

/**
 * Adaptador temporal de {@link TramiteAccessPort} mientras US-01/US-03/US-05 no
 * definan las reglas reales de visibilidad y asignación (research.md §6). Trata
 * como no autorizado cualquier solicitud sin identificador de usuario o con el
 * identificador centinela {@value #USUARIO_NO_AUTORIZADO}, usado por las pruebas
 * para ejercitar el camino 403 (FR-009).
 */
@Component
public class SimulatedTramiteAccessAdapter implements TramiteAccessPort {

    public static final String USUARIO_NO_AUTORIZADO = "no-autorizado";

    @Override
    public boolean estaAutorizado(UUID tramiteId, String usuarioSolicitanteId) {
        return usuarioSolicitanteId != null
                && !usuarioSolicitanteId.isBlank()
                && !USUARIO_NO_AUTORIZADO.equals(usuarioSolicitanteId);
    }
}
