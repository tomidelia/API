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

    /* ----------------------------------------------------------------------
       Ficha tecnica del juego. Todos opcionales: si no vienen, el producto se
       guarda igual y el frontend no muestra ese dato.
       ---------------------------------------------------------------------- */

    @Min(value = 1, message = "El mínimo de jugadores es 1")
    private Integer minPlayers;

    @Min(value = 1, message = "El máximo de jugadores debe ser al menos 1")
    private Integer maxPlayers;

    @Min(value = 1, message = "La duración tiene que ser de al menos 1 minuto")
    private Integer durationMinutes;

    @Min(value = 0, message = "La edad no puede ser negativa")
    private Integer minAge;

    /** Explicacion breve de como se juega. */
    private String howToPlay;


    /** Si viene la lista, reemplaza por completo las fotos del producto. */
    @Size(min = 1, message = "El producto tiene que tener al menos una imagen")
    private List<MultipartFile> images;
}