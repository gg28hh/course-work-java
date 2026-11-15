package com.example.cw.product.services;

import com.example.cw.Command;
import com.example.cw.category.CategoryRepository;
import com.example.cw.category.model.Category;
import com.example.cw.exceptions.CategoryNotFoundException;
import com.example.cw.exceptions.ErrorMessages;
import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.mapper.ProductMapper;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import com.example.cw.product.model.UpdateProductCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UpdateProductService implements Command<UpdateProductCommand, ProductDTO> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public UpdateProductService(
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
    public ResponseEntity<ProductDTO> execute(UpdateProductCommand command) {

        Product existingProduct = productRepository.findById(command.getId())
                .orElseThrow(ProductNotFoundException::new);

        Product updatedProduct = command.getProduct();

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());

        existingProduct.setCategories(
                resolveCategories(updatedProduct.getCategories())
        );

        Product savedProduct = productRepository.save(existingProduct);

        return ResponseEntity.status(HttpStatus.OK)
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