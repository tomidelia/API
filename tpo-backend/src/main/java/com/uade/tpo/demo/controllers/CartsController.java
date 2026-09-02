package com.uade.tpo.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.User;
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
 * El carrito es siempre el del usuario logueado: el id sale del token, nunca de
 * la URL. Asi nadie puede leer ni modificar el carrito de otra persona.
 */
@RestController
@RequestMapping("carts")
public class CartsController {

    @Autowired
    private CartService cartService;

    /** Devuelve el carrito del usuario logueado. */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) throws UserNotFoundException {
        return ResponseEntity.ok(cartService.getCart(user.getId()));
    }

    /** Agrega un producto al carrito validando que tenga stock. */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest cartItemRequest)
            throws UserNotFoundException, ProductNotFoundException, InsufficientStockException {

        return ResponseEntity.ok(cartService.addItem(user.getId(), cartItemRequest));
    }

    /** Modifica la cantidad de una linea del carrito. */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest cartItemRequest)
            throws UserNotFoundException, CartItemNotFoundException, InsufficientStockException {

        return ResponseEntity.ok(cartService.updateItem(user.getId(), itemId, cartItemRequest));
    }

    /** Elimina una linea del carrito. */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId)
            throws UserNotFoundException, CartItemNotFoundException {

        return ResponseEntity.ok(cartService.removeItem(user.getId(), itemId));
    }

    /** Vacia el carrito completo. */
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal User user) throws UserNotFoundException {
        return ResponseEntity.ok(cartService.clearCart(user.getId()));
    }
}
