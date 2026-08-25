package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 400 en el GlobalExceptionHandler. */
public class InsufficientStockException extends Exception {

    public InsufficientStockException(String message) {
        super(message);
    }
}
