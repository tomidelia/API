package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.entity.dto.CartItemUpdateRequest;
import com.uade.tpo.demo.entity.dto.CartResponse;
import com.uade.tpo.demo.exceptions.CartItemNotFoundException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;

public interface CartService {

    public CartResponse getCart(Long userId) throws UserNotFoundException;

    public CartResponse addItem(Long userId, CartItemRequest cartItemRequest)
            throws UserNotFoundException, ProductNotFoundException, InsufficientStockException;

    public CartResponse updateItem(Long userId, Long itemId, CartItemUpdateRequest cartItemRequest)
            throws UserNotFoundException, CartItemNotFoundException, InsufficientStockException;

    public CartResponse removeItem(Long userId, Long itemId)
            throws UserNotFoundException, CartItemNotFoundException;

    public CartResponse clearCart(Long userId) throws UserNotFoundException;
}
