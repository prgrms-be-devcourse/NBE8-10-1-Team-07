package com.back.domain.customer.customer.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "customer")
public class Customer extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    public Customer(String email) {
        this.email = email;
    }
}