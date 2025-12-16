package com.back.domain.order.order.service;

import com.back.domain.order.order.dto.OrderUpdateDto;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService; // 테스트 대상

    @Mock
    private OrderRepository orderRepository; // Mock 객체 (가짜)

    // 테스트에 사용할 샘플 데이터 (엔티티)
    private Order testOrder;
    private Long testOrderId = 1L;

    @BeforeEach
    void setUp() {
        // Order 엔티티는 Customer 정보가 필요하지만, 테스트를 단순화하기 위해 최소 필드만 설정
        testOrder = new Order();
        testOrder.setId(testOrderId);
        testOrder.setShippingAddress("기존 주소");
        testOrder.setShippingCode("12345");
        testOrder.setOrderStatus(OrderStatus.PAID); // 수정 가능한 초기 상태
    }

    // --- U (Update) 성공 테스트 ---

    @Test
    @DisplayName("성공: 유효한 ID와 PAID 상태에서 주소와 우편번호 수정")
    void updateOrderShippingInfo_Success_PaidStatus() {
        // Given
        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setShippingAddress("새로운 서울 주소");
        updateDto.setShippingCode("00001");

        // Mocking: findById 호출 시 testOrder 반환 설정
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // When
        orderService.updateOrderShippingInfo(testOrderId, updateDto);

        // Then
        // 엔티티의 필드가 실제로 변경되었는지 검증 (JPA 변경 감지 동작 확인)
        assertThat(testOrder.getShippingAddress()).isEqualTo("새로운 서울 주소");
        assertThat(testOrder.getShippingCode()).isEqualTo("00001");
        assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID); // 상태는 유지
    }

    // --- U (Update) 실패 테스트 ---

    @Test
    @DisplayName("실패: 존재하지 않는 주문 ID 수정 시 예외 발생")
    void updateOrderShippingInfo_Fail_NotFound() {
        // Given
        Long invalidOrderId = 99L;
        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setShippingAddress("새 주소");
        updateDto.setShippingCode("00001");

        // Mocking: 존재하지 않는 ID 호출 시 Optional.empty() 반환 설정
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderShippingInfo(invalidOrderId, updateDto);
        }, "존재하지 않는 주문 ID입니다.");
    }

    @Test
    @DisplayName("실패: 배송 준비 중(PREPARING) 상태 주문 수정 시 예외 발생")
    void updateOrderShippingInfo_Fail_PreparingStatus() {
        // Given
        testOrder.setOrderStatus(OrderStatus.PREPARING); // 수정 불가능 상태로 변경
        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setShippingAddress("새 주소");
        updateDto.setShippingCode("00001");

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            orderService.updateOrderShippingInfo(testOrderId, updateDto);
        }, "주문 상태 [PREPARING]에서는 배송 정보를 수정할 수 없습니다.");
    }
}