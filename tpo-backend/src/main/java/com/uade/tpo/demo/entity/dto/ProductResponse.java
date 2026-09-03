package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer discount;
    private BigDecimal finalPrice;
    private Integer stock;

    /** Indica si el producto tiene stock disponible. */
    private boolean available;

    private Long categoryId;
    private String categoryDescription;

    /**
     * Imagenes devueltas en Base64 para poder mostrarlas desde el frontend.
     */
    private List<String> images;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .finalPrice(product.getFinalPrice())
                .stock(product.getStock())
                .available(product.isAvailable())
                .categoryId(product.getCategory().getId())
                .categoryDescription(product.getCategory().getDescription())
                .images(product.getImages().stream()
                        .map(ProductResponse::toBase64)
                        .collect(Collectors.toList()))
                .build();
    }

    private static String toBase64(ProductImage productImage) {
        try {
            Blob blob = productImage.getImage();
            byte[] bytes = blob.getBytes(1, (int) blob.length());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo leer la imagen del producto", e);
        }
    }
}