package com.back.domain.order.order.controller;

import com.back.domain.customer.customer.repository.CustomerRepository;
import com.back.domain.customer.customer.entity.Customer;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderItem;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderItemRepository;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.product.product.entity.Product;
import jakarta.persistence.EntityManager;
import com.back.domain.product.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 전체 컨텍스트 로드 및 MockMvc 자동 설정
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // application-test.yml(H2) 쓰게
@Transactional
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired OrderRepository orderRepository;
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired CustomerRepository customerRepository;
    @Autowired ProductRepository productRepository;
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
    @DisplayName("DELETE /api/orders/{id} 호출 시 주문이 삭제되고(orderItems도 함께 삭제됨) 200 응답을 준다")
    void deleteOrder_success() throws Exception {
        // 1) given: 테스트 데이터 저장
        Customer customer = new Customer();
        customer.setEmail("tes12t@test.com");
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
        mockMvc.perform(delete("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                // 3) then: 응답 검증
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("주문이 취소(삭제)되었습니다."));

        // 4) then: DB 삭제 검증
        em.flush();
        em.clear();

        assertThat(orderRepository.existsById(orderId)).isFalse();

        // order_item도 같이 삭제됐는지 확인 (cascade + orphanRemoval 기대)
        Long orderItemCount = em.createQuery("select count(oi) from OrderItem oi", Long.class)
                .getSingleResult();
        assertThat(orderItemCount).isEqualTo(0L);
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
    private Product savedProduct1;
    private final String TEST_EMAIL = "new_customer@example.com";
    private final int PRODUCT1_PRICE = 5000;

    @BeforeEach
    void setup() {
        // 테스트에 사용할 상품 데이터를 미리 DB에 저장
        savedProduct1 = productRepository.save(Product.create("통합 테스트 원두", PRODUCT1_PRICE, "테스트용 설명"));

        // 테스트 격리를 위해 매번 Customer는 생성되지 않도록 초기화
        customerRepository.findByEmail(TEST_EMAIL).ifPresent(customerRepository::delete);
    }

    @Test
    @DisplayName("t1: 주문 생성 (성공)")
    void t1_createOrderSuccess() throws Exception {

        // 요청 본문 JSON 구성
        String requestBody = String.format("""
                {
                    "email": "%s",
                    "shippingAddress": "서울시 강남구",
                    "shippingCode": "12345",
                    "items": [
                        {
                            "productId": %d,
                            "quantity": 2
                        }
                    ]
                }
                """, TEST_EMAIL, savedProduct1.getId());

        // When: 주문 생성 POST 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        // Then: HTTP 응답 검증
        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(status().isCreated()) // RsData 반환 시 기본 200 OK
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("주문이 생성되었습니다."))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.data.totalAmount").value(PRODUCT1_PRICE * 2)); // 5000 * 2 = 10000

        // 추가 검증: DB에 주문이 실제로 생성되었는지 확인
        Optional<Order> createdOrder = orderRepository.findTopByOrderByIdDesc();

        if (createdOrder.isEmpty()) {
            throw new AssertionError("DB에 주문이 생성되지 않았습니다.");
        }

        Order order = createdOrder.get();
        resultActions.andExpect(jsonPath("$.data.id").value(order.getId()));

        // 추가 검증: Customer가 생성되었는지 확인 (Service 로직 검증)
        customerRepository.findByEmail(TEST_EMAIL)
                .orElseThrow(() -> new AssertionError("Customer가 생성되지 않았습니다."));
    }

    @Test
    @DisplayName("t2: 주문 생성 (유효성 검사 실패 - 배송지 누락)")
    void t2_createOrderInvalidValidation() throws Exception {
        // 배송지(shippingAddress)를 빈 문자열로 요청 (DTO에 NotBlank 또는 Size 검증이 있다고 가정)
        String requestBody = String.format("""
                {
                    "email": "%s",
                    "shippingAddress": "",
                    "shippingCode": "12345",
                    "items": [
                        {
                            "productId": %d,
                            "quantity": 1
                        }
                    ]
                }
                """, TEST_EMAIL, savedProduct1.getId());

        // When: 유효하지 않은 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        // Then: 400 Bad Request와 응답 본문 검증 (전역 에러 핸들러의 형식에 맞춰 예상)
        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
        // .andExpect(jsonPath("$.msg").value(Matchers.containsString("shippingAddress")));
    }

    @Test
    @DisplayName("t3: 주문 생성 (존재하지 않는 상품 ID)")
    void t3_createOrderProductNotFound() throws Exception {
        long nonExistentProductId = 999L;

        String requestBody = String.format("""
                {
                    "email": "%s",
                    "shippingAddress": "주소",
                    "shippingCode": "12345",
                    "items": [
                        {
                            "productId": %d,
                            "quantity": 1
                        }
                    ]
                }
                """, TEST_EMAIL, nonExistentProductId);

        // When: 존재하지 않는 상품 ID로 주문 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        // Then: Service에서 던진 IllegalArgumentException에 의해 500 Internal Server Error 발생 예상
        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("t4: 유효성 검사 실패 - 이메일 null/빈값 (NotBlank)")
    void t4_createOrderInvalidValidation_EmptyEmail() throws Exception {
        // email 필드에 null 또는 빈 문자열을 전송
        String requestBody = String.format("""
                {
                    "email": null, // 또는 ""
                    "shippingAddress": "유효한 주소",
                    "shippingCode": "12345",
                    "items": [
                        { "productId": %d, "quantity": 1 }
                    ]
                }
                """, savedProduct1.getId());

        ResultActions resultActions = mvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody.replace("null", "\"\"")) // JSON에서 null 대신 "" 전송
                )
                .andDo(print());

        // Then: 400 Bad Request
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
        // 전역 에러 핸들러가 없으면 응답 본문의 상세 내용은 Spring 기본 에러 메시지로 나옵니다.
    }

    @Test
    @DisplayName("t5: 유효성 검사 실패 - 배송지 Min 미만 (Size)")
    void t5_createOrderInvalidValidation_ShortAddress() throws Exception {
        // shippingAddress에 최소 길이(예: 5) 미만의 짧은 문자열 전송
        String requestBody = String.format("""
                {
                    "email": "%s",
                    "shippingAddress": "짧은", // 4자
                    "shippingCode": "12345",
                    "items": [
                        { "productId": %d, "quantity": 1 }
                    ]
                }
                """, TEST_EMAIL, savedProduct1.getId());

        ResultActions resultActions = mvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        // Then: 400 Bad Request
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("t6: 유효성 검사 실패 - 주문 품목 Max 초과 (Max)")
    void t6_createOrderInvalidValidation_MaxQuantity() throws Exception {
        // quantity 필드에 최대 허용 수량(예: 100) 초과 값 전송
        String requestBody = String.format("""
                {
                    "email": "%s",
                    "shippingAddress": "유효한 주소입니다",
                    "shippingCode": "12345",
                    "items": [
                        {
                            "productId": %d,
                            "quantity": 101 // Max(100) 초과 가정
                        }
                    ]
                }
                """, TEST_EMAIL, savedProduct1.getId());

        ResultActions resultActions = mvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        // Then: 400 Bad Request
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }

    @Test
    @DisplayName("t7: 유효성 검사 실패 - items 리스트 null/empty (NotEmpty)")
    void t7_createOrderInvalidValidation_EmptyItemsList() throws Exception {
        // 1. Items 리스트 자체를 JSON에서 누락시키거나 null로 보냄
        String requestBodyNullItems = String.format("""
                {
                    "email": "%s",
                    "shippingAddress": "유효한 주소",
                    "shippingCode": "12345"
                    // "items" 필드 누락
                }
                """, TEST_EMAIL);

        ResultActions resultActionsNull = mvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBodyNullItems)
                )
                .andDo(print());

        // Then: 400 Bad Request (JSON 바인딩 또는 @Valid 실패)
        resultActionsNull
                .andExpect(status().isBadRequest());

        // 2. Items 리스트를 빈 리스트로 보냄 (@NotEmpty 실패 가정)
        String requestBodyEmptyItems = String.format("""
                {
                    "email": "%s",
                    "shippingAddress": "유효한 주소",
                    "shippingCode": "12345",
                    "items": []
                }
                """, TEST_EMAIL);

        ResultActions resultActionsEmpty = mvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBodyEmptyItems)
                )
                .andDo(print());

        // Then: 400 Bad Request
        resultActionsEmpty
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"));
    }
}