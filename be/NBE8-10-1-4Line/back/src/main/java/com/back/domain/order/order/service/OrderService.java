package com.back.domain.order.order.service;

import com.back.domain.order.order.dto.OrderProductDetailDto;
import com.back.domain.order.order.dto.OrderProductSummaryDto;
import com.back.domain.order.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderItemRepository orderItemRepository;

    public List<OrderProductSummaryDto> getProductSummaries(String email) {
        return orderItemRepository.findProductSummaries(email);
    }

    public List<OrderProductDetailDto> getProductDetails(String email, Long productId) {
        return orderItemRepository.findProductDetails(email, productId);
    }
}
