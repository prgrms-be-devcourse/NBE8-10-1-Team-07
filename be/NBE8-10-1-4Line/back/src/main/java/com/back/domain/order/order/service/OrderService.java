package com.back.domain.order.order.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.order.dto.OrderProductDetailDto;
import com.back.domain.order.order.dto.OrderProductSummaryDto;
import com.back.domain.order.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Optional<Order> findById(long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public void delete(Order order) {
        orderRepository.delete(order);
    }


    @Transactional(readOnly = true)
    public List<OrderProductSummaryDto> getProductSummaries(String email) {
        return orderItemRepository.findProductSummaries(email);
    }
    @Transactional(readOnly = true)
    public List<OrderProductDetailDto> getProductDetails(String email, Long productId) {
        return orderItemRepository.findProductDetails(email, productId);
    }
}