package org.ups.gestiondocumental.tramite.application.port.out;

import org.ups.gestiondocumental.tramite.domain.EventoTramite;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida hacia la persistencia de eventos de trámite.
 * {@code guardar} existe para que las pruebas puedan sembrar eventos directamente
 * (research.md §6), ya que US-01/US-03/US-05 aún no implementan los flujos de
 * escritura completos.
 */
public interface EventoTramiteRepository {

    List<EventoTramite> buscarPorTramiteOrdenadoDesc(UUID tramiteId);

    void guardar(EventoTramite evento);
}
