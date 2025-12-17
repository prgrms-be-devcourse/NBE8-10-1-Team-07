package com.back.domain.order.order.service;

import com.back.domain.customer.customer.entity.Customer;
import com.back.domain.customer.customer.repository.CustomerRepository;
import com.back.domain.order.order.dto.*;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderItem;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.order.dto.OrderProductDetailDto;
import com.back.domain.order.order.dto.OrderProductSummaryDto;
import com.back.domain.order.order.repository.OrderItemRepository;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    private static final Set<OrderStatus> EDITABLE_STATUSES = Set.of(
            OrderStatus.ORDERED,
            OrderStatus.PAID
    );

    public Order create(String email, String shippingAddress, String shippingCode, List<OrderCreateRequestDto.OrderItemRequest> items) {
        Customer customer = getOrCreateCustomer(email);
        List<OrderItem> orderItems = createOrderItems(items);
        Order order = Order.create(
                customer,
                shippingAddress,
                shippingCode,
                orderItems
        );
        return orderRepository.save(order);
    }

    // 헬퍼 함수
    private Customer getOrCreateCustomer(String email) {
        return customerRepository.findByEmail(email)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer(email);
                    return customerRepository.save(newCustomer);
                });
    }

    private List<OrderItem> createOrderItems(List<OrderCreateRequestDto.OrderItemRequest> items) {
        return items.stream()
                .<OrderItem>map(itemReq -> {
                    Long productId = itemReq.productId();
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemReq.productId()));
                    return OrderItem.create(product, itemReq.quantity());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public void delete(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        if (order.getOrderStatus() != OrderStatus.ORDERED) {
            throw new IllegalStateException("접수 상태의 주문만 취소(삭제)할 수 있습니다.");
        }

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