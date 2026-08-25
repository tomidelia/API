package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 404 en el GlobalExceptionHandler. */
public class CartItemNotFoundException extends Exception {

    public CartItemNotFoundException(String message) {
        super(message);
    }
}
