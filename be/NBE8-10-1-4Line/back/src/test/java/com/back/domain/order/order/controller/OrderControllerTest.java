package com.back.domain.order.order.controller;

import com.back.domain.customer.customer.entity.Customer;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderItem;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.product.product.entity.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // application-test.yml(H2) 쓰게
@Transactional
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired OrderRepository orderRepository;

    @Test
    @DisplayName("DELETE /api/orders/{id} 호출 시 주문이 삭제되고(orderItems도 함께 삭제됨) 200 응답을 준다")
    void deleteOrder_success() throws Exception {
        // 1) given: 테스트 데이터 저장
        Customer customer = new Customer();
        customer.setEmail("test@test.com");
        em.persist(customer);

        Product product = new Product();
        product.setName("Colombia");
        product.setPrice(12000);
        product.setDescription("desc");
        em.persist(product);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderTime(LocalDateTime.now());
        order.setTotalAmount(24000);
        order.setOrderStatus(OrderStatus.ORDERED);
        order.setShippingAddress("Seoul");
        order.setShippingCode("12345"); // 엔티티가 String이면 String
        em.persist(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        em.persist(item);

        // 양방향 리스트도 맞춰주면 더 안전 (현재 매핑상 필수는 아니지만 권장)
        order.getOrderItems().add(item);

        em.flush();
        em.clear();

        long orderId = order.getId();
        assertThat(orderRepository.existsById(orderId)).isTrue();

        // 2) when: DELETE 호출
        mockMvc.perform(delete("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                // 3) then: 응답 검증
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value(orderId + "번 주문이 삭제되었습니다."));

        // 4) then: DB 삭제 검증
        em.flush();
        em.clear();

        assertThat(orderRepository.existsById(orderId)).isFalse();

        // order_item도 같이 삭제됐는지 확인 (cascade + orphanRemoval 기대)
        Long orderItemCount = em.createQuery("select count(oi) from OrderItem oi", Long.class)
                .getSingleResult();
        assertThat(orderItemCount).isEqualTo(0L);
    }
}
