package com.back.domain.order.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderUpdateDto {

    @NotBlank(message = "배송 주소는 필수로 입력해야 합니다.")
    @Size(max = 255, message = "배송 주소는 최대 255자를 초과할 수 없습니다.")
    private String shippingAddress;

    @NotBlank(message = "우편번호는 필수로 입력해야 합니다.")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String shippingCode;
}
