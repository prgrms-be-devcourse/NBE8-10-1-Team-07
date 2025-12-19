package com.back.domain.customer.customer.controller;

import com.back.domain.customer.customer.dto.CustomerEmailExistsResponse;
import com.back.domain.customer.customer.service.CustomerService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "CustomerController", description = "API 고객 컨트롤러")
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/exists")
    @Operation(summary = "이메일로 고객 존재 여부 확인")
    public RsData<CustomerEmailExistsResponse> exists(
            @RequestParam
            @NotBlank(message = "이메일은 필수 입력값입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ) {

        CustomerEmailExistsResponse data = customerService.existsByEmail(email);

        return new RsData<>(
                "200-1",
                "이메일 확인 완료",
                data
        );
    }
}