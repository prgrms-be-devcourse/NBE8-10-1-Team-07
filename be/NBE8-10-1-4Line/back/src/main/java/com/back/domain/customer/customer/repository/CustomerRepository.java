package com.back.domain.customer.customer.repository;

import com.back.domain.customer.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
}