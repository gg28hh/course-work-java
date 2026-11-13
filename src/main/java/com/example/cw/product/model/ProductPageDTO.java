package com.example.cw.product.model;

import java.util.List;

public record ProductPageDTO(
        List<ProductDTO> products,
        long totalProducts
) {}