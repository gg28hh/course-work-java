package com.example.cw.product.services;

import com.example.cw.Command;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.model.Category;
import com.example.cw.exceptions.CategoryNotFoundException;
import com.example.cw.exceptions.ErrorMessages;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.mapper.ProductMapper;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class CreateProductService implements Command<Product, ProductDTO> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public CreateProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<ProductDTO> execute(Product product) {

        product.setCategories(resolveCategories(product.getCategories()));

        Product savedProduct = productRepository.save(product);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productMapper.toDTO(savedProduct));
    }

    private Set<Category> resolveCategories(Set<Category> categories) {

        Set<Category> resolvedCategories = new HashSet<>();

        if (categories == null) {
            return resolvedCategories;
        }

        for (Category category : categories) {

            if (category == null || category.getId() == null) {
                throw new CategoryNotFoundException(ErrorMessages.CATEGORY_NOT_FOUND);
            }

            Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() ->
                            new CategoryNotFoundException(ErrorMessages.CATEGORY_NOT_FOUND));

            resolvedCategories.add(existingCategory);
        }

        return resolvedCategories;
    }
}