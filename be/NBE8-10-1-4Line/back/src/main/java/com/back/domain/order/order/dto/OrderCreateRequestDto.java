package com.back.domain.order.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OrderCreateRequestDto(
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Size(min = 2, max = 100, message = "이메일은 2자 이상 100자 이하여야 합니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "이메일 형식이 올바르지 않습니다."
        )
        String email,

        @NotBlank(message = "배송지 주소는 필수 입력값입니다.")
        @Size(min = 2, max = 255, message = "배송지 주소는 2자 이상 255자 이하여야 합니다.")
        String shippingAddress,

        @NotBlank(message = "우편번호는 필수 입력값입니다.")
        @Size(min = 5, max = 5, message = "우편번호는 5자리여야 합니다.")
        String shippingCode,

        @Valid
        List<OrderItemRequest> items
) {
        public record OrderItemRequest(
                @NotNull(message = "상품 ID는 필수입니다.")
                Long productId,

                @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
                int quantity
        ) {}
}
