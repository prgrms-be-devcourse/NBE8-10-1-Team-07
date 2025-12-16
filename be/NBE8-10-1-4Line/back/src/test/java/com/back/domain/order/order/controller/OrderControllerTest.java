package com.back.domain.order.order.controller;

import com.back.domain.customer.customer.entity.Customer;
import com.back.domain.customer.repository.CustomerRepository;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderItem;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderItemRepository;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired MockMvc mockMvc;

    @Autowired CustomerRepository customerRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;

    private final String email = "test@test.com";

    private Product coffee;
    private Product cake;

    @BeforeEach
    void setUp() {
        // 각 테스트마다 데이터 깨끗하게
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        Customer c = new Customer();
        c.setEmail(email);
        customerRepository.save(c);

        coffee = new Product();
        coffee.setName("커피");
        coffee.setPrice(5000);
        coffee.setDescription("아메리카노");
        productRepository.save(coffee);

        cake = new Product();
        cake.setName("케이크");
        cake.setPrice(6000);
        cake.setDescription("치즈케이크");
        productRepository.save(cake);

        // 주문 2건 생성 (같은 상품을 여러 번 사는 케이스 포함)
        Order o1 = new Order();
        o1.setCustomer(c);
        o1.setOrderTime(LocalDateTime.now().minusDays(1));
        o1.setOrderStatus(OrderStatus.ORDERED);
        o1.setShippingAddress("서울시 강남구");
        o1.setShippingCode("CJ111");
        o1.setTotalAmount(16000);
        orderRepository.save(o1);

        OrderItem o1Coffee2 = new OrderItem();
        o1Coffee2.setOrder(o1);
        o1Coffee2.setProduct(coffee);
        o1Coffee2.setQuantity(2);
        orderItemRepository.save(o1Coffee2);

        OrderItem o1Cake1 = new OrderItem();
        o1Cake1.setOrder(o1);
        o1Cake1.setProduct(cake);
        o1Cake1.setQuantity(1);
        orderItemRepository.save(o1Cake1);

        Order o2 = new Order();
        o2.setCustomer(c);
        o2.setOrderTime(LocalDateTime.now());
        o2.setOrderStatus(OrderStatus.DELIVERED);
        o2.setShippingAddress("서울시 마포구");
        o2.setShippingCode("CJ222");
        o2.setTotalAmount(5000);
        orderRepository.save(o2);

        OrderItem o2Coffee1 = new OrderItem();
        o2Coffee1.setOrder(o2);
        o2Coffee1.setProduct(coffee);
        o2Coffee1.setQuantity(1);
        orderItemRepository.save(o2Coffee1);
    }

    @Test
    @DisplayName("요약 바 조회: 상품별로 묶여서 총수량/총금액이 계산된다")
    void summary_shouldGroupByProduct() throws Exception {
        mockMvc.perform(get("/api/orders/summary")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))

                // 커피: 2 + 1 = 3개, 5000 * 3 = 15000
                .andExpect(jsonPath("$.data[?(@.productName=='커피')].totalQuantity", contains(3)))
                .andExpect(jsonPath("$.data[?(@.productName=='커피')].totalAmount", contains(15000)))

                // 케이크: 1개, 6000원
                .andExpect(jsonPath("$.data[?(@.productName=='케이크')].totalQuantity", contains(1)))
                .andExpect(jsonPath("$.data[?(@.productName=='케이크')].totalAmount", contains(6000)));
    }

    @Test
    @DisplayName("상품 상세 조회: 특정 상품(productId)의 주문 상세 리스트가 내려온다")
    void detail_shouldReturnOrdersForProduct() throws Exception {
        mockMvc.perform(get("/api/orders/summary/{productId}", coffee.getId())
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))

                // 각 row에 필수 필드가 있는지
                .andExpect(jsonPath("$.data[0].orderId").isNumber())
                .andExpect(jsonPath("$.data[0].orderTime").isNotEmpty())
                .andExpect(jsonPath("$.data[0].orderStatus").isNotEmpty())
                .andExpect(jsonPath("$.data[0].shippingAddress").isNotEmpty())
                .andExpect(jsonPath("$.data[0].shippingCode").isNotEmpty())

                // 커피 가격 5000, 수량/소계 검증 (2개짜리 주문이 있고, 1개짜리 주문이 있음)
                .andExpect(jsonPath("$.data[*].pricePerItem", everyItem(is(5000))))
                .andExpect(jsonPath("$.data[*].quantity", containsInAnyOrder(2, 1)))
                .andExpect(jsonPath("$.data[*].subTotal", containsInAnyOrder(10000, 5000)));
    }

    @Test
    @DisplayName("데이터가 없으면 data는 빈 배열로 내려온다")
    void summary_empty_whenNoOrders() throws Exception {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();

        mockMvc.perform(get("/api/orders/summary")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
