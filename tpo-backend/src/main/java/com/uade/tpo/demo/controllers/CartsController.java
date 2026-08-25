package com.uade.tpo.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.entity.dto.CartItemUpdateRequest;
import com.uade.tpo.demo.entity.dto.CartResponse;
import com.uade.tpo.demo.exceptions.CartItemNotFoundException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
import com.uade.tpo.demo.service.CartService;

import jakarta.validation.Valid;

/**
 * El userId viaja en la URL hasta que este la capa de seguridad. Cuando
 * integremos JWT el carrito se resuelve con el usuario del token.
 */
@RestController
@RequestMapping("carts")
public class CartsController {

    @Autowired
    private CartService cartService;

    /** Devuelve el carrito del usuario (lo crea vacio si todavia no existe). */
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) throws UserNotFoundException {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    /** Agrega un producto al carrito validando que tenga stock. */
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemRequest cartItemRequest)
            throws UserNotFoundException, ProductNotFoundException, InsufficientStockException {

        return ResponseEntity.ok(cartService.addItem(userId, cartItemRequest));
    }

    /** Modifica la cantidad de una linea del carrito. */
    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest cartItemRequest)
            throws UserNotFoundException, CartItemNotFoundException, InsufficientStockException {

        return ResponseEntity.ok(cartService.updateItem(userId, itemId, cartItemRequest));
    }

    /** Elimina una linea del carrito. */
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long userId,
            @PathVariable Long itemId)
            throws UserNotFoundException, CartItemNotFoundException {

        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

    /** Vacia el carrito completo. */
    @DeleteMapping("/{userId}")
    public ResponseEntity<CartResponse> clearCart(@PathVariable Long userId) throws UserNotFoundException {
        return ResponseEntity.ok(cartService.clearCart(userId));
    }
}
