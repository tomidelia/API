package com.uade.tpo.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Busqueda y filtrado del catalogo. Todos los filtros son opcionales: si el
     * parametro llega en null la condicion se ignora, asi un unico endpoint
     * resuelve "traer todo" y "traer filtrado".
     */
    @Query("select p from Product p "
            + "where p.active = true "
            + "and (:categoryId is null or p.category.id = :categoryId) "
            + "and (:sellerId is null or p.seller.id = :sellerId) "
            + "and (:minPrice is null or p.price >= :minPrice) "
            + "and (:maxPrice is null or p.price <= :maxPrice) "
            + "and (:onlyAvailable = false or p.stock > 0) "
            + "and (:search is null or lower(p.name) like lower(concat('%', :search, '%')) "
            + "     or lower(p.description) like lower(concat('%', :search, '%')))")
    Page<Product> search(@Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("onlyAvailable") boolean onlyAvailable,
            @Param("search") String search,
            Pageable pageable);

    List<Product> findBySellerIdAndActiveTrue(Long sellerId);

    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
}
