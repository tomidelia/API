package com.uade.tpo.demo.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.uade.tpo.demo.entity.dto.DiscountRequest;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;
import com.uade.tpo.demo.entity.dto.ProductUpdateRequest;
import com.uade.tpo.demo.entity.dto.StockRequest;
import com.uade.tpo.demo.exceptions.CategoryNotFoundException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;

public interface ProductService {

    public Page<ProductResponse> getProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            boolean onlyAvailable, String search, PageRequest pageRequest);

    public ProductResponse getProductById(Long productId) throws ProductNotFoundException;

    public ProductResponse createProduct(ProductRequest productRequest) throws CategoryNotFoundException;

    public ProductResponse updateProduct(Long productId, ProductUpdateRequest productRequest)
            throws ProductNotFoundException, CategoryNotFoundException;

    public ProductResponse updateStock(Long productId, StockRequest stockRequest)
            throws ProductNotFoundException;

    public ProductResponse updateDiscount(Long productId, DiscountRequest discountRequest)
            throws ProductNotFoundException;

    public void deleteProduct(Long productId) throws ProductNotFoundException;
}
