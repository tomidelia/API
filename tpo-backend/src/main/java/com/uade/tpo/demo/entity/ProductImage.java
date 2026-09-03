package com.uade.tpo.demo.entity;

import java.sql.Blob;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "product" })
@Entity
@Table(name = "product_image")
public class ProductImage {

    public ProductImage() {
    }

    public ProductImage(Blob image, Product product) {
        this.image = image;
        this.product = product;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Archivo de imagen almacenado como Blob en la base de datos.
     */
    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private Blob image;

    // Muchas imagenes pueden pertenecer a un mismo producto.
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}