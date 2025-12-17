package com.back.domain.order.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderUpdateDto {

    // 수정할 주소
    @NotBlank(message = "배송 주소는 필수로 입력해야 합니다.")
    @Size(max = 255, message = "배송 주소는 최대 255자를 초과할 수 없습니다.")
    private String shippingAddress;

    // 수정할 우편번호
    @NotBlank(message = "우편 번호는 필수로 입력해야 합니다.")
    @Size(max = 5, message = "우편번호는 최대 5자를 초과할 수 없습니다.")
    private String shippingCode;


}