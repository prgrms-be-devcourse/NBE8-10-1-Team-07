package com.back.domain.order.order.service;

import com.back.domain.customer.customer.entity.Customer;
import com.back.domain.customer.customer.repository.CustomerRepository;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.order.repository.OrderItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * 테스트용 주문 생성 헬퍼
     */
    private Order createOrder(OrderStatus status) {
        Customer customer = new Customer();
        customer.setEmail("test@test.com");
        customerRepository.save(customer);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderStatus(status);
        order.setOrderTime(LocalDateTime.now());
        order.setTotalAmount(10000);
        order.setShippingAddress("서울시 강남구");
        order.setShippingCode("12345");

        return orderRepository.save(order);
    }

    @Test
    @DisplayName("ORDERED 상태의 주문은 삭제된다")
    void delete_ordered_order_success() {
        // given
        Order order = createOrder(OrderStatus.ORDERED);
        Long orderId = order.getId();

        // when
        orderService.delete(orderId);

        // then
        assertThat(orderRepository.findById(orderId)).isEmpty();
    }

    @Test
    @DisplayName("ORDERED 상태가 아니면 주문 삭제가 불가능하다")
    void delete_not_ordered_order_fail() {
        // given
        Order order = createOrder(OrderStatus.SHIPPING);

        // when & then
        assertThatThrownBy(() -> orderService.delete(order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("접수 상태의 주문만");
    }

    @Test
    @DisplayName("존재하지 않는 주문을 삭제하면 예외가 발생한다")
    void delete_non_existing_order_fail() {
        // given
        Long invalidOrderId = 9999L;

        // when & then
        assertThatThrownBy(() -> orderService.delete(invalidOrderId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문이 존재하지 않습니다");
    }
}
