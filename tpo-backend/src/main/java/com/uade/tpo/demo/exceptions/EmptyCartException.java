package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 400 en el GlobalExceptionHandler. */
public class EmptyCartException extends Exception {

    public EmptyCartException(String message) {
        super(message);
    }
}
