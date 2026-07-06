package org.ups.gestiondocumental.tramite.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ups.gestiondocumental.tramite.application.AccesoTramiteNoAutorizadoException;
import org.ups.gestiondocumental.tramite.application.TramiteNoEncontradoException;
import org.ups.gestiondocumental.tramite.infrastructure.web.openapi.model.ErrorResponse;

@RestControllerAdvice
public class TramiteExceptionHandler {

    @ExceptionHandler(AccesoTramiteNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoNoAutorizado(AccesoTramiteNoAutorizadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("TRAMITE_NO_AUTORIZADO", ex.getMessage()));
    }

    @ExceptionHandler(TramiteNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarTramiteNoEncontrado(TramiteNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("TRAMITE_NO_ENCONTRADO", ex.getMessage()));
    }
}
