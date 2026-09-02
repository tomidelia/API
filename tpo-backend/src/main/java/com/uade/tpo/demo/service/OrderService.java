package com.uade.tpo.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.uade.tpo.demo.entity.dto.OrderResponse;
import com.uade.tpo.demo.exceptions.EmptyCartException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;

public interface OrderService {

    /** Cierra el carrito del usuario: valida stock, calcula el total y descuenta. */
    public OrderResponse checkout(Long userId)
            throws UserNotFoundException, EmptyCartException, InsufficientStockException;

    public Page<OrderResponse> getOrdersByUser(Long userId, PageRequest pageRequest) throws UserNotFoundException;

    /** Solo devuelve la orden si pertenece al usuario que la pide. */
    public OrderResponse getOrderById(Long orderId, Long userId) throws OrderNotFoundException;
}
