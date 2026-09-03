package com.uade.tpo.demo.controllers;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.uade.tpo.demo.entity.dto.ErrorResponse;
import com.uade.tpo.demo.exceptions.CartItemNotFoundException;
import com.uade.tpo.demo.exceptions.CategoryNotFoundException;
import com.uade.tpo.demo.exceptions.DuplicateUserException;
import com.uade.tpo.demo.exceptions.EmptyCartException;
import com.uade.tpo.demo.exceptions.ForbiddenActionException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
import com.uade.tpo.demo.exceptions.InvalidImageException;

/**
 * Centraliza el manejo de errores: cada excepcion de negocio se traduce a un
 * status code HTTP con un cuerpo JSON siempre igual, para que el cliente
 * (Insomnia hoy, el frontend despues) pueda leer el motivo del rechazo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ ProductNotFoundException.class, CategoryNotFoundException.class,
            UserNotFoundException.class, OrderNotFoundException.class, CartItemNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ InsufficientStockException.class, EmptyCartException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(Exception ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUser(Exception ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Login fallido. Se responde un mensaje generico a proposito: no se le
     * aclara al cliente si lo que estuvo mal fue el email o la contrasenia.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Email o contrasenia incorrectos");
    }

    /** Errores de las validaciones declarativas de los DTO (@NotBlank, @Min...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(InvalidImageException.class)
public ResponseEntity<ErrorResponse> handleInvalidImage(
        InvalidImageException exception) {

    return build(HttpStatus.BAD_REQUEST, exception.getMessage());
}
}
