package org.ups.gestiondocumental.tramite.application.port.out;

import java.util.UUID;

/**
 * Puerto de salida para verificar autorización sobre un trámite (FR-009).
 * Reutiliza las reglas de acceso que definen US-01/US-03/US-05; mientras esas
 * historias no estén implementadas, se resuelve con un adaptador simulado
 * (research.md §6).
 */
public interface TramiteAccessPort {

    boolean estaAutorizado(UUID tramiteId, String usuarioSolicitanteId);
}
