package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 404 en el GlobalExceptionHandler. */
public class OrderNotFoundException extends Exception {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
