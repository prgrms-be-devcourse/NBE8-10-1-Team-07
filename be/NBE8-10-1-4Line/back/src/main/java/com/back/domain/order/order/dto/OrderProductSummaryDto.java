package com.back.domain.order.order.dto;

public record OrderProductSummaryDto(
        Long productId,
        String productName,
        Long totalQuantity,
        Long totalAmount
) {
}
