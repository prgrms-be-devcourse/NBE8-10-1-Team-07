package com.back.domain.product.product.service;

import com.back.domain.product.product.dto.ProductDto;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    public Long count() {
        return productRepository.count();
    }

    public Product create(String name, int price, String description) {
        Product product = Product.create(name, price, description);
        return productRepository.save(product);
    }
    public List<ProductDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductDto::new)
                .toList();
    }
}