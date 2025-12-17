package com.back.domain.product.product.dto;

import com.back.domain.product.product.entity.Product;

public record ProductDto(
        long id,
        String name,
        int price,
        String description
) {
    public ProductDto(Product product) {
        this(product.getId(), product.getName(), product.getPrice(), product.getDescription());
    }
}
