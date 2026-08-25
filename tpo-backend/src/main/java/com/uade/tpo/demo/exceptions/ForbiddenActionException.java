package com.uade.tpo.demo.exceptions;

/**
 * Se traduce a un HTTP 403. Por ahora valida a mano que el vendedor sea el
 * duenio del producto; cuando integremos seguridad esto lo resuelve el token.
 */
public class ForbiddenActionException extends Exception {

    public ForbiddenActionException(String message) {
        super(message);
    }
}
