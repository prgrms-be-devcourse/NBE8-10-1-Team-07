package com.back.domain.customer.customer.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 기타 고객 정보 필드 (예: name, phone_number 등)는 필요에 따라 추가하세요.


}
