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

import com.uade.tpo.demo.entity.dto.DiscountRequest;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;
import com.uade.tpo.demo.entity.dto.ProductUpdateRequest;
import com.uade.tpo.demo.entity.dto.StockRequest;
import com.uade.tpo.demo.exceptions.CategoryNotFoundException;
import com.uade.tpo.demo.exceptions.ForbiddenActionException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
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
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) String search) {

        PageRequest pageRequest = (page == null || size == null)
                ? PageRequest.of(0, Integer.MAX_VALUE)
                : PageRequest.of(page, size);

        return ResponseEntity.ok(productService.getProducts(
                categoryId, sellerId, minPrice, maxPrice, onlyAvailable, search, pageRequest));
    }

    /** Detalle del producto: imagen, descripcion, precio y disponibilidad. */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId)
            throws ProductNotFoundException {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    /** Alta de una publicacion por parte de un vendedor. */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest)
            throws CategoryNotFoundException, UserNotFoundException {

        ProductResponse result = productService.createProduct(productRequest);
        return ResponseEntity.created(URI.create("/products/" + result.getId())).body(result);
    }

    /**
     * Modificacion de la publicacion. El sellerId viaja por query param hasta que
     * este la capa de seguridad; despues sale del token del usuario autenticado.
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @RequestParam Long sellerId,
            @Valid @RequestBody ProductUpdateRequest productRequest)
            throws ProductNotFoundException, CategoryNotFoundException, ForbiddenActionException {

        return ResponseEntity.ok(productService.updateProduct(productId, sellerId, productRequest));
    }

    /** Manejo de stock por parte del vendedor duenio del producto. */
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long productId,
            @RequestParam Long sellerId,
            @Valid @RequestBody StockRequest stockRequest)
            throws ProductNotFoundException, ForbiddenActionException {

        return ResponseEntity.ok(productService.updateStock(productId, sellerId, stockRequest));
    }

    /** Gestion de descuentos sobre un producto individual. */
    @PatchMapping("/{productId}/discount")
    public ResponseEntity<ProductResponse> updateDiscount(
            @PathVariable Long productId,
            @RequestParam Long sellerId,
            @Valid @RequestBody DiscountRequest discountRequest)
            throws ProductNotFoundException, ForbiddenActionException {

        return ResponseEntity.ok(productService.updateDiscount(productId, sellerId, discountRequest));
    }

    /** Baja de la publicacion. */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @RequestParam Long sellerId)
            throws ProductNotFoundException, ForbiddenActionException {

        productService.deleteProduct(productId, sellerId);
        return ResponseEntity.noContent().build();
    }
}
