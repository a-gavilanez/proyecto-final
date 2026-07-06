package org.ups.gestiondocumental.tramite.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ups.gestiondocumental.tramite.domain.TipoEvento;

import java.time.Instant;
import java.util.UUID;

/**
 * Tabla única de eventos (research.md §4): un evento por fila, con columnas de
 * delegación (sumilla, colaboradorAsignado) nulas cuando tipoEvento no es
 * DELEGACION. {@code secuencia} es una clave técnica autoincremental usada
 * únicamente como desempate estable de orden de inserción (research.md §5); no
 * forma parte de la identidad de negocio, que es {@code id}.
 */
@Entity
@Table(name = "evento_tramite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventoTramiteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "secuencia", nullable = false, updatable = false)
    private Long secuencia;

    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "tramite_id", nullable = false)
    private UUID tramiteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 20)
    private TipoEvento tipoEvento;

    @Column(name = "actor_responsable", nullable = false)
    private String actorResponsable;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "sumilla", length = 4000)
    private String sumilla;

    @Column(name = "colaborador_asignado")
    private String colaboradorAsignado;

    @Column(name = "etiqueta_generica")
    private String etiquetaGenerica;

    public EventoTramiteJpaEntity(UUID id, UUID tramiteId, TipoEvento tipoEvento, String actorResponsable,
                                   Instant fechaHora, String sumilla, String colaboradorAsignado,
                                   String etiquetaGenerica) {
        this.id = id;
        this.tramiteId = tramiteId;
        this.tipoEvento = tipoEvento;
        this.actorResponsable = actorResponsable;
        this.fechaHora = fechaHora;
        this.sumilla = sumilla;
        this.colaboradorAsignado = colaboradorAsignado;
        this.etiquetaGenerica = etiquetaGenerica;
    }
}
