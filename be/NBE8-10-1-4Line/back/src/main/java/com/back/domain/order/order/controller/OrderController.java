// be/.../OrderQueryController.java
package com.back.domain.order.order.controller;

import com.back.domain.order.order.dto.OrderProductDetailDto;
import com.back.domain.order.order.dto.OrderProductSummaryDto;
import com.back.domain.order.order.service.OrderService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "OrderQueryController", description = "주문 조회(상품별 요약/상세) API")
public class OrderController {

    private final OrderService orderService;

    // ✅ 요약 바 리스트
    // 예: GET /api/orders/summary?email=test@test.com
    @GetMapping("/summary")
    public RsData<List<OrderProductSummaryDto>> summaries(@RequestParam String email) {
        return new RsData<>("200-1", "주문 요약(상품별) 조회 성공",
                orderService.getProductSummaries(email));
    }

    // ✅ 바 클릭 시 상세 리스트
    // 예: GET /api/orders/summary/3?email=test@test.com
    @GetMapping("/summary/{productId}")
    public RsData<List<OrderProductDetailDto>> details(
            @RequestParam String email,
            @PathVariable Long productId
    ) {
        return new RsData<>("200-1", "주문 상세(상품별) 조회 성공",
                orderService.getProductDetails(email, productId));
    }
}
