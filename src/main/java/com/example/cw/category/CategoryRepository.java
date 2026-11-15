package com.example.cw.category;

import com.example.cw.category.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository
        extends JpaRepository<Category, Integer> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Query(
            value = """
            SELECT c
            FROM Category c
            WHERE
                (:name IS NULL OR :name = ''
                    OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """,
            countQuery = """
            SELECT COUNT(c)
            FROM Category c
            WHERE
                (:name IS NULL OR :name = ''
                    OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """
    )
    Page<Category> searchCategories(
            @Param("name") String name,
            Pageable pageable
    );
}
