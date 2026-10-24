package com.uade.tpo.demo.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "category", "images" })
@Entity
public class Product {

    public Product() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    /** Descuento del producto expresado en porcentaje (0 a 100). */
    @Column(nullable = false)
    private Integer discount = 0;

    /* ----------------------------------------------------------------------
       Ficha técnica del juego.

       Son los datos que cualquiera mira antes de comprar un juego de mesa:
       para cuántos es, cuánto dura una partida y desde qué edad se puede
       jugar. Van en la base y no escritos en el frontend, porque son datos
       del producto: cuando la tienda publique un juego nuevo los carga igual
       que el precio o el stock.

       Son opcionales: un producto viejo o uno cargado a las apuradas puede no
       tenerlos, y el frontend simplemente no muestra ese dato.
       ---------------------------------------------------------------------- */

    /** Cantidad mínima de jugadores. */
    private Integer minPlayers;

    /** Cantidad máxima de jugadores. */
    private Integer maxPlayers;

    /** Cuánto dura una partida, en minutos. */
    private Integer durationMinutes;

    /** Edad desde la que se recomienda. */
    private Integer minAge;

    /** Explicación breve de cómo se juega. */
    @Column(length = 4000)
    private String howToPlay;

    /**
     * Baja lógica. Al eliminar una publicación el producto deja de verse en el
     * catálogo, pero se conserva para no romper las órdenes ya realizadas que
     * lo referencian.
     */
    @Column(nullable = false)
    private Boolean active = true;

    // Relación ManyToOne: muchos productos pertenecen a una categoría.
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Relación OneToMany: un producto tiene una o más fotos.
    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    /** Precio final ya aplicado el descuento del producto. */
    // ESLABÓN 1 de 3 del total: precio con el descuento ya aplicado.
    public BigDecimal getFinalPrice() {
        if (discount == null || discount == 0)
            return price.setScale(2, RoundingMode.HALF_UP);

        BigDecimal factor = BigDecimal.valueOf(100 - discount).divide(BigDecimal.valueOf(100));
        return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    /** Indica si el producto tiene stock disponible. */
    public boolean isAvailable() {
        return stock != null && stock > 0;
    }
}
