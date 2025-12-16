package com.back.domain.order.order.dto;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String email,
        LocalDateTime orderTime,
        int totalAmount,
        OrderStatus orderStatus,
        String shippingAddress,
        String shippingCode,
        List<OrderItemDto> orderItems
) {
    public OrderDto(Order order) {
        this(
                order.getId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCustomer().getEmail(),
                order.getOrderTime(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getShippingAddress(),
                order.getShippingCode(),

                // OrderItem 리스트 변환 로직
                order.getOrderItems().stream()
                        .map(OrderItemDto::new) // 각 OrderItem을 OrderItemDto로 변환
                        .toList()
        );
    }
}