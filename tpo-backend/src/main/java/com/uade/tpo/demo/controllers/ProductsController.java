package com.uade.tpo.demo.controllers;

import java.math.BigDecimal;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.uade.tpo.demo.entity.dto.DiscountRequest;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;
import com.uade.tpo.demo.entity.dto.ProductUpdateRequest;
import com.uade.tpo.demo.entity.dto.StockRequest;
import com.uade.tpo.demo.exceptions.CategoryNotFoundException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("products")
public class ProductsController {

    @Autowired
    private ProductService productService;

    /**
     * Catalogo con paginado y filtros opcionales.
     * Ej: /products?page=0&size=10&categoryId=1&minPrice=5000&search=catan
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) String search) {

        PageRequest pageRequest = (page == null || size == null)
                ? PageRequest.of(0, Integer.MAX_VALUE)
                : PageRequest.of(page, size);

        return ResponseEntity.ok(productService.getProducts(
                categoryId, minPrice, maxPrice, onlyAvailable, search, pageRequest));
    }

    /** Detalle del producto: imagenes, descripcion, precio y disponibilidad. */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId)
            throws ProductNotFoundException {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    /** Alta de una publicacion. Con seguridad, queda restringido al rol ADMIN. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @ModelAttribute ProductRequest productRequest)
        throws CategoryNotFoundException {

        ProductResponse result = productService.createProduct(productRequest);
        return ResponseEntity.created(URI.create("/products/" + result.getId())).body(result);
    }

    /** Modificacion de la publicacion. */
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long productId,
        @Valid @ModelAttribute ProductUpdateRequest productRequest)
            throws ProductNotFoundException, CategoryNotFoundException {

        return ResponseEntity.ok(productService.updateProduct(productId, productRequest));
    }

    /** Manejo del stock del producto. */
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockRequest stockRequest) throws ProductNotFoundException {

        return ResponseEntity.ok(productService.updateStock(productId, stockRequest));
    }

    /** Gestion de descuentos sobre un producto individual. */
    @PatchMapping("/{productId}/discount")
    public ResponseEntity<ProductResponse> updateDiscount(
            @PathVariable Long productId,
            @Valid @RequestBody DiscountRequest discountRequest) throws ProductNotFoundException {

        return ResponseEntity.ok(productService.updateDiscount(productId, discountRequest));
    }

    /** Baja de la publicacion. */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) throws ProductNotFoundException {

        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
