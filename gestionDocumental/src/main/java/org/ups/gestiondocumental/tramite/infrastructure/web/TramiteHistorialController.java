package org.ups.gestiondocumental.tramite.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.gestiondocumental.tramite.application.port.in.ConsultarLineaDeTiempoUseCase;
import org.ups.gestiondocumental.tramite.infrastructure.web.openapi.api.HistorialApi;
import org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.LineaDeTiempoResponse;

import java.util.UUID;

@RestController
public class TramiteHistorialController implements HistorialApi {

    private final ConsultarLineaDeTiempoUseCase consultarLineaDeTiempoUseCase;
    private final EventoTramiteWebMapper mapper;

    public TramiteHistorialController(ConsultarLineaDeTiempoUseCase consultarLineaDeTiempoUseCase,
                                       EventoTramiteWebMapper mapper) {
        this.consultarLineaDeTiempoUseCase = consultarLineaDeTiempoUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<LineaDeTiempoResponse> consultarLineaDeTiempo(UUID tramiteId, String xUsuarioId) {
        var eventos = consultarLineaDeTiempoUseCase.consultar(tramiteId, xUsuarioId);
        return ResponseEntity.ok(mapper.aRespuesta(tramiteId, eventos));
    }
}
