
package com.example.cw.services;

import com.example.cw.Command;
import com.example.cw.exceptions.ProductNotFoundException;
import com.example.cw.product.ProductRepository;
import com.example.cw.product.model.Product;
import com.example.cw.product.model.ProductDTO;
import com.example.cw.product.model.UpdateProductCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateProductService implements Command<UpdateProductCommand, ProductDTO> {

    private final ProductRepository productRepository;

    public UpdateProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ResponseEntity<ProductDTO> execute(UpdateProductCommand command) {

        Optional<Product> productOptional =
                productRepository.findById(command.getId());

        if (productOptional.isPresent()) {

            Product product = command.getProduct();
            product.setId(command.getId());

            productRepository.save(product);

            return ResponseEntity.ok(new ProductDTO(product));
        }

        throw new ProductNotFoundException();
    }
}
