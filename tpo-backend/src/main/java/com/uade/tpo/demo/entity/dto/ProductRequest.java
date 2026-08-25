package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotBlank(message = "La descripcion del producto es obligatoria")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Max(value = 100, message = "El descuento no puede superar el 100%")
    private Integer discount = 0;

    @NotNull(message = "La categoria es obligatoria")
    private Long categoryId;

    /**
     * Hasta que este la capa de seguridad el vendedor viaja en el body.
     * Cuando integremos JWT se toma del usuario autenticado.
     */
    @NotNull(message = "El vendedor es obligatorio")
    private Long sellerId;

    /** Una o mas fotos del producto (URL o imagen en base64). */
    private List<String> images = new ArrayList<>();
}
