package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 409 en el GlobalExceptionHandler. */
public class DuplicateUserException extends Exception {

    public DuplicateUserException(String message) {
        super(message);
    }
}
