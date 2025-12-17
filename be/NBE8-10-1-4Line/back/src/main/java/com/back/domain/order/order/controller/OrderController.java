package com.back.domain.order.order.controller;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.dto.OrderProductDetailDto;
import com.back.domain.order.order.dto.OrderProductSummaryDto;
import com.back.domain.order.order.service.OrderService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "OrderController", description = "API 주문 컨트롤러")
public class OrderController {
    private final OrderService orderService;

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "주문 삭제")
    public RsData<Void> delete(@PathVariable long id) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다. id=" + id));

        orderService.delete(order);

        return new RsData<>(
                "200-1",
                "%d번 주문이 삭제되었습니다.".formatted(id)
        );
    }

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