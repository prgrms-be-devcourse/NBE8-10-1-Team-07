package com.back.domain.order.order.controller;

import com.back.domain.order.order.dto.OrderDto;
import com.back.domain.order.order.dto.OrderUpdateDto;
import com.back.domain.order.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "OrderController", description = "API 주문 컨트롤러")
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "주문 배송 정보 수정", description = "주소와 우편번호 수정")
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDto> updateOrderShippingInfo(
            @PathVariable("orderId") Long orderId,
            @RequestBody OrderUpdateDto request) {

        OrderDto updatedOrder = orderService.updateOrderShippingInfo(orderId, request);

        return ResponseEntity.ok(updatedOrder);
    }
}