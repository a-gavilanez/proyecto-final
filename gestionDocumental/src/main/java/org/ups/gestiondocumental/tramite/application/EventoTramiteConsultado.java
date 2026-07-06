package org.ups.gestiondocumental.tramite.application;

import org.ups.gestiondocumental.tramite.domain.EventoTramite;

/**
 * Vista de un {@link EventoTramite} para la consulta de línea de tiempo: añade el
 * indicador {@code pendiente}, calculado por el caso de uso y no persistido
 * (FR-007, FR-008; data-model.md).
 */
public record EventoTramiteConsultado(EventoTramite evento, boolean pendiente) {
}
