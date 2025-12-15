package com.back.global.jpa.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order")
@Getter
@NoArgsConstructor
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; // 개별 주문 건의 ID

    // Customer 엔티티와의 N:1 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_email", referencedColumnName = "email", nullable = false)
    private CustomerEntity customer; // 주문한 고객

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime; // 주문 접수 시각

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount; // 주문 총 금액

    @Column(name = "order_status", length = 50, nullable = false)
    private String orderStatus; // 주문 상태 (e.g., '접수', '배송 준비 중')

    @Column(name = "shipping_address", length = 255, nullable = false)
    private String shippingAddress; // 고객의 배송 주소

    @Column(name = "shipping_code", nullable = false)
    private Integer shippingCode; // 배송지 우편 번호

    @Builder
    public OrderEntity(CustomerEntity customer, LocalDateTime orderTime, Integer totalAmount, String orderStatus, String shippingAddress, Integer shippingCode) {
        this.customer = customer;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.shippingAddress = shippingAddress;
        this.shippingCode = shippingCode;
    }
}
