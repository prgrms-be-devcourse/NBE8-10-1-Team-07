package com.back.domain.order.order.service;

import com.back.domain.order.order.dto.OrderUpdateDto;
import com.back.domain.order.order.dto.OrderDto;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.order.dto.OrderProductDetailDto;
import com.back.domain.order.order.dto.OrderProductSummaryDto;
import com.back.domain.order.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private static final Set<OrderStatus> EDITABLE_STATUSES = Set.of(
            OrderStatus.ORDERED,
            OrderStatus.PAID
    );

    @Transactional(readOnly = true)
    public Optional<Order> findById(long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public void delete(Order order) {
        orderRepository.delete(order);
    }


    @Transactional(readOnly = true)
    public List<OrderProductSummaryDto> getProductSummaries(String email) {
        return orderItemRepository.findProductSummaries(email);
    }
    @Transactional(readOnly = true)
    public List<OrderProductDetailDto> getProductDetails(String email, Long productId) {
        return orderItemRepository.findProductDetails(email, productId);
    }

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