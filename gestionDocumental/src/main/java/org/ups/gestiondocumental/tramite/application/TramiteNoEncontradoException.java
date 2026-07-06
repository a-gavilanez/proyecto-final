package org.ups.gestiondocumental.tramite.application;

import java.util.UUID;

public class TramiteNoEncontradoException extends RuntimeException {

    public TramiteNoEncontradoException(UUID tramiteId) {
        super("No existe un tramite con id " + tramiteId);
    }
}
