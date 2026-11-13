package com.example.cw.category;

import com.example.cw.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryRepository extends
        JpaRepository<Category, Integer>,
        JpaSpecificationExecutor<Category> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}