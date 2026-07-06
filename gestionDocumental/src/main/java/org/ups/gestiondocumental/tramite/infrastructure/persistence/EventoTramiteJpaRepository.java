package org.ups.gestiondocumental.tramite.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoTramiteJpaRepository extends JpaRepository<EventoTramiteJpaEntity, Long> {

    List<EventoTramiteJpaEntity> findByTramiteIdOrderByFechaHoraDescSecuenciaDesc(UUID tramiteId);
}
