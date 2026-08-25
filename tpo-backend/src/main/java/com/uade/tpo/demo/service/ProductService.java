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
import com.uade.tpo.demo.exceptions.ForbiddenActionException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;

public interface ProductService {

    public Page<ProductResponse> getProducts(Long categoryId, Long sellerId, BigDecimal minPrice,
            BigDecimal maxPrice, boolean onlyAvailable, String search, PageRequest pageRequest);

    public ProductResponse getProductById(Long productId) throws ProductNotFoundException;

    public ProductResponse createProduct(ProductRequest productRequest)
            throws CategoryNotFoundException, UserNotFoundException;

    public ProductResponse updateProduct(Long productId, Long sellerId, ProductUpdateRequest productRequest)
            throws ProductNotFoundException, CategoryNotFoundException, ForbiddenActionException;

    public ProductResponse updateStock(Long productId, Long sellerId, StockRequest stockRequest)
            throws ProductNotFoundException, ForbiddenActionException;

    public ProductResponse updateDiscount(Long productId, Long sellerId, DiscountRequest discountRequest)
            throws ProductNotFoundException, ForbiddenActionException;

    public void deleteProduct(Long productId, Long sellerId)
            throws ProductNotFoundException, ForbiddenActionException;
}
