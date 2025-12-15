package com.back.global.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer")
@Getter
@NoArgsConstructor
public class CustomerEntity {

    @Id
    @Column(name = "email", length = 100, nullable = false)
    private String email; // 고객 식별자 (이메일 주소, PK)

    // 기타 고객 정보 필드 (예: name, phone_number 등)는 필요에 따라 추가하세요.

    @Builder
    public CustomerEntity(String email) {
        this.email = email;
    }
}
