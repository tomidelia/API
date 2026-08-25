package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private BigDecimal total;

    public static CartResponse from(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(cart.getItems().stream()
                        .map(CartItemResponse::from)
                        .collect(Collectors.toList()))
                .total(cart.getTotal())
                .build();
    }
}
