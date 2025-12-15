package com.back.global.jpa.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor
public class OrderItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemId; // 주문 항목 ID

    // Order 엔티티와의 N:1 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order; // 속한 주문

    // Product 엔티티와의 N:1 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product; // 주문된 상품

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 주문 수량

    @Builder
    public OrderItemEntity(OrderEntity order, ProductEntity product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
    }
}
