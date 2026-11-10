package com.example.cw.product.services;

import com.example.cw.Command;
import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.mapper.ProductMapper;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import com.example.cw.product.model.UpdateProductCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateProductService implements Command<UpdateProductCommand, ProductDTO> {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public UpdateProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ResponseEntity<ProductDTO> execute(UpdateProductCommand command) {

            Optional<Product> productOptional =
                    productRepository.findById(command.getId());

            if (productOptional.isPresent()) {

                Product existingProduct = productOptional.get();
                Product updatedProduct = command.getProduct();

                existingProduct.setName(updatedProduct.getName());
                existingProduct.setDescription(updatedProduct.getDescription());
                existingProduct.setPrice(updatedProduct.getPrice());
                existingProduct.setStockQuantity(updatedProduct.getStockQuantity());

                Product savedProduct = productRepository.save(existingProduct);

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(productMapper.toDTO(savedProduct));
            }

        throw new ProductNotFoundException();
    }
}
