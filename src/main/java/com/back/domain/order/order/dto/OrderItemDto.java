package com.back.domain.order.order.dto;

import com.back.domain.order.order.entity.OrderItem;

public record OrderItemDto(
        long productId,
        String productName,
        int quantity,
        int pricePerItem, // Product의 price와 동일
        long subTotal
) {
    // 💡 OrderItem 엔티티를 받아 DTO를 생성하는 생성자
    public OrderItemDto(OrderItem item) {
        this(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getProduct().getPrice(), // Product 엔티티에서 가격 가져오기
                (long) item.getProduct().getPrice() * item.getQuantity()
        );
    }
}