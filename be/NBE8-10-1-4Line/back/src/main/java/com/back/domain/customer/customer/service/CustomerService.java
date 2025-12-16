package com.back.domain.customer.customer.service;

import com.back.domain.customer.customer.dto.CustomerEmailExistsResponse;
import com.back.domain.customer.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public CustomerEmailExistsResponse existsByEmail(String email) {
        boolean exists = customerRepository.existsByEmail(email);
        return new CustomerEmailExistsResponse(exists);
    }
}
