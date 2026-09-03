package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Todos los campos son opcionales: solo se actualiza lo que llega. */
@Data
public class ProductUpdateRequest {

    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    private BigDecimal price;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Max(value = 100, message = "El descuento no puede superar el 100%")
    private Integer discount;

    private Long categoryId;

    /** Si viene la lista, reemplaza por completo las fotos del producto. */
    @Size(min = 1, message = "El producto tiene que tener al menos una imagen")
    private List<MultipartFile> images;
}