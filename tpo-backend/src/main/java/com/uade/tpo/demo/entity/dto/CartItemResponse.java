package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;

import com.uade.tpo.demo.entity.CartItem;
import com.uade.tpo.demo.entity.ProductImage;

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
                        : toBase64(item.getProduct().getImages().get(0)))
                .unitPrice(item.getProduct().getFinalPrice())
                .discount(item.getProduct().getDiscount())
                .quantity(item.getQuantity())
                .stock(item.getProduct().getStock())
                .subtotal(item.getSubtotal())
                .build();
    }

    private static String toBase64(ProductImage productImage) {
        try {
            Blob blob = productImage.getImage();
            byte[] bytes = blob.getBytes(1, (int) blob.length());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo leer la imagen del producto", e);
        }
    }
}