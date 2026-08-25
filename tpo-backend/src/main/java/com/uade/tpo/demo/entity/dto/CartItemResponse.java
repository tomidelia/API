package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

import com.uade.tpo.demo.entity.CartItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String image;
    private BigDecimal unitPrice;
    private Integer discount;
    private Integer quantity;
    private Integer stock;
    private BigDecimal subtotal;

    public static CartItemResponse from(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .image(item.getProduct().getImages().isEmpty()
                        ? null
                        : item.getProduct().getImages().get(0).getUrl())
                .unitPrice(item.getProduct().getFinalPrice())
                .discount(item.getProduct().getDiscount())
                .quantity(item.getQuantity())
                .stock(item.getProduct().getStock())
                .subtotal(item.getSubtotal())
                .build();
    }
}
