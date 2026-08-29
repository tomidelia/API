package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.dto.DiscountRequest;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;
import com.uade.tpo.demo.entity.dto.ProductUpdateRequest;
import com.uade.tpo.demo.entity.dto.StockRequest;
import com.uade.tpo.demo.exceptions.CategoryNotFoundException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.repository.CartItemRepository;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;

/**
 * La tienda tiene un unico vendedor: cualquier alta, modificacion o baja la hace el ADMIN. 
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            boolean onlyAvailable, String search, PageRequest pageRequest) {

        String term = (search == null || search.isBlank()) ? null : search.trim();

        return productRepository
                .search(categoryId, minPrice, maxPrice, onlyAvailable, term, pageRequest)
                .map(ProductResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) throws ProductNotFoundException {
        return ProductResponse.from(findActiveProduct(productId));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) throws CategoryNotFoundException {

        Category category = findCategory(productRequest.getCategoryId());

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setDiscount(productRequest.getDiscount() == null ? 0 : productRequest.getDiscount());
        product.setCategory(category);
        product.setActive(true);
        replaceImages(product, productRequest.getImages());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest productRequest)
            throws ProductNotFoundException, CategoryNotFoundException {

        Product product = findActiveProduct(productId);

        if (productRequest.getName() != null)
            product.setName(productRequest.getName());

        if (productRequest.getDescription() != null)
            product.setDescription(productRequest.getDescription());

        if (productRequest.getPrice() != null)
            product.setPrice(productRequest.getPrice());

        if (productRequest.getStock() != null)
            product.setStock(productRequest.getStock());

        if (productRequest.getDiscount() != null)
            product.setDiscount(productRequest.getDiscount());

        if (productRequest.getCategoryId() != null)
            product.setCategory(findCategory(productRequest.getCategoryId()));

        if (productRequest.getImages() != null)
            replaceImages(product, productRequest.getImages());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long productId, StockRequest stockRequest)
            throws ProductNotFoundException {

        Product product = findActiveProduct(productId);
        product.setStock(stockRequest.getStock());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateDiscount(Long productId, DiscountRequest discountRequest)
            throws ProductNotFoundException {

        Product product = findActiveProduct(productId);
        product.setDiscount(discountRequest.getDiscount());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) throws ProductNotFoundException {

        Product product = findActiveProduct(productId);

        // Se saca de los carritos donde este cargado y se le hace una baja logica,
        // para no romper las ordenes ya cerradas que apuntan a este producto.
        cartItemRepository.deleteAll(cartItemRepository.findByProductId(productId));
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findActiveProduct(Long productId) throws ProductNotFoundException {
        return productRepository.findById(productId)
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException("No existe el producto con id " + productId));
    }

    private Category findCategory(Long categoryId) throws CategoryNotFoundException {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("No existe la categoria con id " + categoryId));
    }

    private void replaceImages(Product product, List<String> urls) {
        product.getImages().clear();
        if (urls == null)
            return;

        for (String url : urls) {
            if (url != null && !url.isBlank())
                product.getImages().add(new ProductImage(url.trim(), product));
        }
    }
}
