package org.ups.gestiondocumental.tramite.domain;

/**
 * Detalle adicional de un evento de tipo {@link TipoEvento#DELEGACION}.
 * La sumilla puede estar ausente (FR-006); el colaborador asignado es obligatorio.
 */
public record DetalleDelegacion(String sumilla, String colaboradorAsignado) {

    public DetalleDelegacion {
        if (colaboradorAsignado == null || colaboradorAsignado.isBlank()) {
            throw new IllegalArgumentException("colaboradorAsignado es obligatorio en una delegacion");
        }
    }

    public boolean tieneSumilla() {
        return sumilla != null && !sumilla.isBlank();
    }
}
