package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
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
    /** El enunciado pide indicar cuando el producto no tiene stock. */
    private boolean available;
    private Long categoryId;
    private String categoryDescription;
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
                        .map(ProductImage::getUrl)
                        .collect(Collectors.toList()))
                .build();
    }
}
