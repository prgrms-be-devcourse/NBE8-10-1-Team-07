package com.back.domain.order.order.service;

import com.back.domain.order.order.dto.OrderDto;
import com.back.domain.order.order.dto.OrderUpdateDto;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private static final Set<OrderStatus> EDITABLE_STATUSES = Set.of(
            OrderStatus.ORDERED,
            OrderStatus.PAID
    );

    @Transactional
    public OrderDto updateOrderShippingInfo(Long orderId, OrderUpdateDto request) {

        //주문 엔티티 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID입니다: " + orderId));

        //수정 가능 유무 검증
        if (!EDITABLE_STATUSES.contains(order.getOrderStatus())) {
            throw new IllegalStateException(
                    "주문 상태 [" + order.getOrderStatus() + "]에서는 배송 정보를 수정할 수 없습니다. (PREPARING 이상)"
            );
        }

        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCode(request.getShippingCode());

        return new OrderDto(order);
    }
}