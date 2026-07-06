package org.ups.gestiondocumental.tramite.application;

import java.util.UUID;

public class AccesoTramiteNoAutorizadoException extends RuntimeException {

    public AccesoTramiteNoAutorizadoException(UUID tramiteId) {
        super("El usuario no esta autorizado para consultar el tramite " + tramiteId);
    }
}
