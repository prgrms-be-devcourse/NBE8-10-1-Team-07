package com.back.domain.order.order.entity;

import com.back.domain.customer.customer.entity.Customer;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "customer_email", referencedColumnName = "email")
    private Customer customer;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "shipping_code", nullable = false)
    private String shippingCode;

    @OneToMany(mappedBy = "order", fetch = LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public static Order create(Customer customer, String shippingAddress, String shippingCode, List<OrderItem> orderItems) {
        Order order = new Order();
        order.customer = customer;
        order.orderTime = LocalDateTime.now();
        order.orderStatus = OrderStatus.ORDERED;
        order.shippingAddress = shippingAddress;
        order.shippingCode = shippingCode;
        order.orderItems = orderItems;

        order.calculateTotalAmount(orderItems);

        return order;
    }

    private void calculateTotalAmount(List<OrderItem> items) {
        int total = 0;
        for (OrderItem item : items) {
            // this.orderItems.add(item);
            item.setOrder(this);

            total += item.getProduct().getPrice() * item.getQuantity();
        }
        this.totalAmount = total;
    }
}