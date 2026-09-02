package com.uade.tpo.demo.controllers;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.OrderResponse;
import com.uade.tpo.demo.exceptions.EmptyCartException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
import com.uade.tpo.demo.service.OrderService;

/**
 * Las ordenes son siempre las del usuario logueado: el id sale del token.
 */
@RestController
@RequestMapping("orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    /** Checkout del carrito: calcula el total y descuenta el stock. */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal User user)
            throws UserNotFoundException, EmptyCartException, InsufficientStockException {

        OrderResponse result = orderService.checkout(user.getId());
        return ResponseEntity.created(URI.create("/orders/" + result.getId())).body(result);
    }

    /** Historial de compras del usuario logueado, paginado. */
    @GetMapping("/my")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws UserNotFoundException {

        PageRequest pageRequest = (page == null || size == null)
                ? PageRequest.of(0, Integer.MAX_VALUE)
                : PageRequest.of(page, size);

        return ResponseEntity.ok(orderService.getOrdersByUser(user.getId(), pageRequest));
    }

    /**
     * Detalle de una orden propia. Si la orden es de otro usuario responde 404
     * y no 403, para no confirmarle a nadie que esa orden existe.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) throws OrderNotFoundException {

        return ResponseEntity.ok(orderService.getOrderById(orderId, user.getId()));
    }
}
