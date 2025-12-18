package com.back.domain.order.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OrderCreateRequestDto(
        @NotBlank
        @Size(min = 2, max = 100)
        @Email
        String email,
        @NotBlank
        @Size(min = 2, max = 255)
        String shippingAddress,
        @NotBlank
        @Size(min = 5, max = 5)
        String shippingCode,
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull
            Long productId,
            @Min(1)
            int quantity
    ) {}
}