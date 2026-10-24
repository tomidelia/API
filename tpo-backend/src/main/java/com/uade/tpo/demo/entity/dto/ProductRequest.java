package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotBlank(message = "La descripción del producto es obligatoria")
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

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    /* ----------------------------------------------------------------------
       Ficha técnica del juego. Todos opcionales: si no vienen, el producto se
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

    /** Explicación breve de cómo se juega. */
    private String howToPlay;


    /**
 * Una o más imágenes recibidas mediante multipart/form-data.
 */
@NotEmpty(message = "El producto tiene que tener al menos una imagen")
private List<MultipartFile> images = new ArrayList<>();
}