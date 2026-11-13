package com.example.cw.product.services;

import com.example.cw.Query;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.mapper.ProductMapper;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import com.example.cw.product.model.ProductPageDTO;
import com.example.cw.product.model.ProductSearchCriteria;
import com.example.cw.product.specification.ProductSpecification;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetProductsService
        implements Query<ProductSearchCriteria, ProductPageDTO>{

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public GetProductsService(
            ProductRepository productRepository,
            ProductMapper productMapper) {

        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ResponseEntity<ProductPageDTO> execute(
            ProductSearchCriteria input) {

        if (input.minPrice() != null &&
                input.maxPrice() != null &&
                input.minPrice() > input.maxPrice()) {

            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price!");
        }

        if (input.page() < 0 || input.size() < 1 || input.size() > 100) {
            throw new IllegalArgumentException("Invalid page or size!");
        }

        String sortBy = input.sortBy();

        if (sortBy == null ||
                !List.of("id", "name", "price").contains(sortBy)) {

            throw new IllegalArgumentException("Invalid sorting field!");
        }

        if (input.direction() == null ||
                (!input.direction().equalsIgnoreCase("asc") &&
                        !input.direction().equalsIgnoreCase("desc"))) {

            throw new IllegalArgumentException("Invalid sorting direction!");
        }
        Sort.Direction direction =
                Sort.Direction.fromString(input.direction());

        Pageable pageable = PageRequest.of(
                input.page(),
                input.size(),
                Sort.by(direction, sortBy)
        );

        Specification<Product> specification = ProductSpecification.filter(
                input.name(),
                input.categoryId(),
                input.minPrice(),
                input.maxPrice()
        );

        Page<ProductDTO> products = productRepository
                .findAll(specification, pageable)
                .map(productMapper::toDTO);

        ProductPageDTO result = new ProductPageDTO(products.getContent(), products.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}