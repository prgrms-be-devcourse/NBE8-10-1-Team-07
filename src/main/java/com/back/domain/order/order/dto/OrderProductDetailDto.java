package com.back.domain.order.order.dto;

import com.back.domain.order.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderProductDetailDto(
        Long orderId,
        LocalDateTime orderTime,
        OrderStatus orderStatus,
        String shippingAddress,
        String shippingCode,
        Integer quantity,     // 이 주문에서 해당 상품 수량
        Integer pricePerItem, // 현재는 Product.price 기준
        Long subTotal         // quantity * pricePerItem
) {
}
