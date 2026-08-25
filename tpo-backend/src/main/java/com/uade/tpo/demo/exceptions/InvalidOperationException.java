package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 400 en el GlobalExceptionHandler. */
public class InvalidOperationException extends Exception {

    public InvalidOperationException(String message) {
        super(message);
    }
}
