package com.back.domain.order.order.entity;

public enum OrderStatus {
    ORDERED,    // 주문 완료
    PAID,       // 결제 완료
    PREPARING,  // 배송 준비 중
    SHIPPING,   // 배송 중
    DELIVERED,  // 배송 완료
    CANCELED    // 주문 취소
}