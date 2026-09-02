package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 403 en el GlobalExceptionHandler. */
public class ForbiddenActionException extends Exception {

    public ForbiddenActionException(String message) {
        super(message);
    }
}
