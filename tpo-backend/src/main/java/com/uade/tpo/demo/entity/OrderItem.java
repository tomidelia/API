package com.uade.tpo.demo.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Guarda el precio y el descuento con los que se compro el producto, para que
 * la orden no cambie si despues el vendedor modifica el precio.
 */
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "order", "product" })
@Entity
@Table(name = "order_item")
public class OrderItem {

    public OrderItem() {
    }

    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.discount = product.getDiscount();
        this.subtotal = product.getFinalPrice().multiply(BigDecimal.valueOf(quantity));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacion ManyToOne: muchas lineas pertenecen a una misma orden.
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Relacion ManyToOne: muchas lineas pueden apuntar al mismo producto.
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer discount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
