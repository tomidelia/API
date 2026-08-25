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
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.DiscountRequest;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;
import com.uade.tpo.demo.entity.dto.ProductUpdateRequest;
import com.uade.tpo.demo.entity.dto.StockRequest;
import com.uade.tpo.demo.exceptions.CategoryNotFoundException;
import com.uade.tpo.demo.exceptions.ForbiddenActionException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
import com.uade.tpo.demo.repository.CartItemRepository;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Long categoryId, Long sellerId, BigDecimal minPrice,
            BigDecimal maxPrice, boolean onlyAvailable, String search, PageRequest pageRequest) {

        String term = (search == null || search.isBlank()) ? null : search.trim();

        return productRepository
                .search(categoryId, sellerId, minPrice, maxPrice, onlyAvailable, term, pageRequest)
                .map(ProductResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) throws ProductNotFoundException {
        return ProductResponse.from(findActiveProduct(productId));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest)
            throws CategoryNotFoundException, UserNotFoundException {

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "No existe la categoria con id " + productRequest.getCategoryId()));

        User seller = userRepository.findById(productRequest.getSellerId())
                .orElseThrow(() -> new UserNotFoundException(
                        "No existe el usuario con id " + productRequest.getSellerId()));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setDiscount(productRequest.getDiscount() == null ? 0 : productRequest.getDiscount());
        product.setCategory(category);
        product.setSeller(seller);
        product.setActive(true);
        replaceImages(product, productRequest.getImages());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, Long sellerId, ProductUpdateRequest productRequest)
            throws ProductNotFoundException, CategoryNotFoundException, ForbiddenActionException {

        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);

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

        if (productRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "No existe la categoria con id " + productRequest.getCategoryId()));
            product.setCategory(category);
        }

        if (productRequest.getImages() != null)
            replaceImages(product, productRequest.getImages());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long productId, Long sellerId, StockRequest stockRequest)
            throws ProductNotFoundException, ForbiddenActionException {

        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);
        product.setStock(stockRequest.getStock());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateDiscount(Long productId, Long sellerId, DiscountRequest discountRequest)
            throws ProductNotFoundException, ForbiddenActionException {

        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);
        product.setDiscount(discountRequest.getDiscount());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId)
            throws ProductNotFoundException, ForbiddenActionException {

        Product product = findActiveProduct(productId);
        validateOwner(product, sellerId);

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

    /** Hasta que este la capa de seguridad, el duenio de la publicacion se valida a mano. */
    private void validateOwner(Product product, Long sellerId) throws ForbiddenActionException {
        if (!product.getSeller().getId().equals(sellerId))
            throw new ForbiddenActionException(
                    "El usuario " + sellerId + " no es el vendedor de la publicacion " + product.getId());
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
