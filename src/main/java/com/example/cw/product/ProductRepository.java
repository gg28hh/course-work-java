package com.example.cw.product;

import com.example.cw.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Integer> {

    @Query(
            value = """
            SELECT DISTINCT p
            FROM Product p
            LEFT JOIN p.categories c
            WHERE
                (:name IS NULL OR :name = ''
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
                AND (:categoryId IS NULL OR c.id = :categoryId)
                AND (:minPrice IS NULL OR p.price >= :minPrice)
                AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                AND (
                    :inStock IS NULL
                    OR (:inStock = TRUE AND p.stockQuantity > 0)
                    OR (:inStock = FALSE AND p.stockQuantity = 0)
                )
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p)
            FROM Product p
            LEFT JOIN p.categories c
            WHERE
                (:name IS NULL OR :name = ''
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
                AND (:categoryId IS NULL OR c.id = :categoryId)
                AND (:minPrice IS NULL OR p.price >= :minPrice)
                AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                AND (
                    :inStock IS NULL
                    OR (:inStock = TRUE AND p.stockQuantity > 0)
                    OR (:inStock = FALSE AND p.stockQuantity = 0)
                )
            """
    )
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable
    );
}