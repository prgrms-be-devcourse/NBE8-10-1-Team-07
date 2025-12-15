package com.back.domain.order.order.entity;

import com.back.domain.customer.customer.entity.Customer;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_email", referencedColumnName = "email")
    private Customer customer;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "order_status", nullable = false, length = 50)
    private String orderStatus;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "shipping_code", nullable = false)
    private int shippingCode;
}
