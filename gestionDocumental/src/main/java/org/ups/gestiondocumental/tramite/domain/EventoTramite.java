package org.ups.gestiondocumental.tramite.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Un evento individual del ciclo de vida de un trámite (FR-001 a FR-003).
 * Regla de validación clave (data-model.md): un evento DELEGACION MUST tener
 * detalleDelegacion; cualquier otro tipo MUST NOT tenerlo.
 */
public record EventoTramite(
        UUID id,
        UUID tramiteId,
        TipoEvento tipoEvento,
        String actorResponsable,
        Instant fechaHora,
        DetalleDelegacion detalleDelegacion,
        String etiquetaGenerica
) {

    public EventoTramite {
        if (id == null) {
            throw new IllegalArgumentException("id es obligatorio");
        }
        if (tramiteId == null) {
            throw new IllegalArgumentException("tramiteId es obligatorio");
        }
        if (tipoEvento == null) {
            throw new IllegalArgumentException("tipoEvento es obligatorio");
        }
        if (actorResponsable == null || actorResponsable.isBlank()) {
            throw new IllegalArgumentException("actorResponsable es obligatorio");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("fechaHora es obligatoria");
        }
        if (tipoEvento == TipoEvento.DELEGACION && detalleDelegacion == null) {
            throw new IllegalArgumentException("un evento DELEGACION requiere detalleDelegacion");
        }
        if (tipoEvento != TipoEvento.DELEGACION && detalleDelegacion != null) {
            throw new IllegalArgumentException("solo un evento DELEGACION puede tener detalleDelegacion");
        }
    }
}
