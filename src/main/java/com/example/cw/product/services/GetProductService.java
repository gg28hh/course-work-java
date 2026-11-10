package com.example.cw.product.services;

import com.example.cw.Query;
import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.mapper.ProductMapper;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class GetProductService implements Query<Integer, ProductDTO> {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public GetProductService(ProductRepository productRepository, ProductMapper productMapper ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

        @Override
        public ResponseEntity<ProductDTO> execute(Integer input) {

            Product product = productRepository.findById(input)
                    .orElseThrow(ProductNotFoundException::new);

            return ResponseEntity.ok(productMapper.toDTO(product));
        }
    }


