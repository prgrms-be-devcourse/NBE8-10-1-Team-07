package com.back.domain.order.order.controller;

import com.back.domain.order.order.dto.OrderDto;
import com.back.domain.order.order.dto.OrderUpdateDto;
import com.back.domain.order.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        // JPA 관련 자동 설정을 꺼서 Hibernate 에러를 방지합니다.
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        }
)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP 요청을 시뮬레이션

    @Autowired
    private ObjectMapper objectMapper; // JSON 직렬화/역직렬화

    @MockBean
    private OrderService orderService; // Service 계층은 Mocking

    private final Long testOrderId = 1L;
    private final String API_URL = "/api/orders/{orderId}";

    // --- U (Update) 성공 테스트 ---

    @Test
    @DisplayName("성공: PUT /api/orders/{orderId} - 주소 수정 성공 및 200 응답")
    void updateOrderShippingInfo_Success() throws Exception {
        // Given
        OrderUpdateDto requestDto = new OrderUpdateDto();
        requestDto.setShippingAddress("새로운 주소");
        requestDto.setShippingCode("00001");

        // Service가 반환할 Mock 응답 (OrderDto는 R 담당자가 정의한 DTO)
        OrderDto mockResponse = new OrderDto(testOrderId, null, null, "test@mail.com", null,
                10000, null, "새로운 주소", "00001", null);

        // Mocking: Service 호출 시 Mock 응답 반환 설정
        when(orderService.updateOrderShippingInfo(eq(testOrderId), any(OrderUpdateDto.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(put(API_URL, testOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                .andExpect(status().isOk()) // HTTP 200 OK 확인
                .andExpect(jsonPath("$.shippingAddress").value("새로운 주소")) // JSON 필드 검증
                .andExpect(jsonPath("$.shippingCode").value("00001"));
    }

    // --- U (Update) 실패 테스트 ---

    @Test
    @DisplayName("실패: 존재하지 않는 주문 ID 수정 시 400 Bad Request 응답")
    void updateOrderShippingInfo_Fail_NotFound() throws Exception {
        // Given
        OrderUpdateDto requestDto = new OrderUpdateDto();
        requestDto.setShippingAddress("새 주소");
        requestDto.setShippingCode("00001");

        // Mocking: Service 호출 시 IllegalArgumentException (400에 해당) 발생 설정
        when(orderService.updateOrderShippingInfo(eq(testOrderId), any(OrderUpdateDto.class)))
                .thenThrow(new IllegalArgumentException("존재하지 않는 주문 ID입니다."));

        // When & Then
        mockMvc.perform(put(API_URL, testOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                .andExpect(status().isBadRequest()); // HTTP 400 Bad Request 확인 (ExceptionHandler를 통해)
    }
}