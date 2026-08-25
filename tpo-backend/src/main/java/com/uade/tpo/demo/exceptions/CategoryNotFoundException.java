package com.uade.tpo.demo.exceptions;

/** Se traduce a un HTTP 404 en el GlobalExceptionHandler. */
public class CategoryNotFoundException extends Exception {

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
