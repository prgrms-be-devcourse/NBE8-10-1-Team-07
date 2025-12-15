package com.back.global.jpa.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; // 상품 고유 번호

    @Column(name = "name", length = 100, nullable = false)
    private String name; // 상품 이름 (원두 종류)

    @Column(name = "price", nullable = false)
    private Integer price; // 상품 단가

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 상품 상세 설명

    @Builder
    public ProductEntity(String name, Integer price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }
}
