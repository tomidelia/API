package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 404 en el GlobalExceptionHandler. */
public class UserNotFoundException extends Exception {

    public UserNotFoundException(String message) {
        super(message);
    }
}
