package com.back.domain.order.order.controller;

import com.back.domain.order.order.dto.*;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.service.OrderService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "OrderController", description = "API 주문 컨트롤러")
public class OrderController {
    private final OrderService orderService;

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소")
    public RsData<Void> delete(@PathVariable Long orderId) {
        orderService.delete(orderId);
        return new RsData<>("200-1", "주문이 취소(삭제)되었습니다.", null);
    }

    @PostMapping
    @Operation(summary = "주문 생성")
    public RsData<OrderDto> create(@Valid @RequestBody OrderCreateRequestDto req) {
        Order order = orderService.create(
                req.email(),
                req.shippingAddress(),
                req.shippingCode(),
                req.items()
        );

        return new RsData<>(
                "201-1",
                "주문이 생성되었습니다.",
                new OrderDto(order)
        );
    }

    // ✅ 요약 바 리스트
    // 예: GET /api/orders/summary?email=test@test.com
    @GetMapping("/summary")
    @Operation(summary = "주문 요약 조회")
    public RsData<List<OrderProductSummaryDto>> summaries(@RequestParam String email) {
        return new RsData<>("200-1", "주문 요약(상품별) 조회 성공",
                orderService.getProductSummaries(email));
    }

    // ✅ 바 클릭 시 상세 리스트
    // 예: GET /api/orders/summary/3?email=test@test.com
    @GetMapping("/summary/{productId}")
    @Operation(summary = "주문 상세 조회")
    public RsData<List<OrderProductDetailDto>> details(
            @RequestParam String email,
            @PathVariable Long productId
    ) {
        return new RsData<>("200-1", "주문 상세(상품별) 조회 성공",
                orderService.getProductDetails(email, productId));
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "주문 배송 정보 수정", description = "주소와 우편번호 수정")
    public RsData<OrderDto> updateOrderShippingInfo(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody OrderUpdateDto request) {

        OrderDto updatedOrder = orderService.updateOrderShippingInfo(orderId, request);

        return new RsData<>(
                "200-1",
                "%d번 주문의 배송 정보가 수정되었습니다.".formatted(orderId),
                updatedOrder
        );
    }

    @Operation(summary = "주문 상태 변경", description = "주문의 상태를 변경하고 고객에게 알림 메일을 보냅니다.")
    @PatchMapping("/{orderId}/status")
    public RsData<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam com.back.domain.order.order.entity.OrderStatus status) {

        orderService.updateOrderStatus(orderId, status);

        return new RsData<>(
                "200-1",
                "주문 상태가 %s로 변경되었으며 알림 메일이 발송되었습니다.".formatted(status)
        );
    }

}