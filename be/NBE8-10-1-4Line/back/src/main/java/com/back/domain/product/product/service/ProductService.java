package com.back.domain.product.product.service;

import com.back.domain.product.product.dto.ProductDto;
import com.back.domain.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<ProductDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductDto::new)
                .toList();
    }
}
