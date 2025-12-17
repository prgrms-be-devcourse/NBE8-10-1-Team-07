package com.back.domain.customer.customer.controller;

import com.back.domain.customer.customer.entity.Customer;
import com.back.domain.customer.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("이메일이 존재하면 exists=true 반환")
    void exists_true() throws Exception {
        // given
        Customer customer = new Customer();
        customer.setEmail("test@refhub.com");
        customerRepository.save(customer);

        // when & then
        mockMvc.perform(
                        get("/api/customers/exists")
                                .param("email", "test@refhub.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 exists=false 반환")
    void exists_false() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/api/customers/exists")
                                .param("email", "notexist@refhub.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    @DisplayName("email 파라미터가 없으면 400 반환")
    void exists_email_missing() throws Exception {
        mockMvc.perform(
                        get("/api/customers/exists")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("email이 빈 문자열이면 400 반환")
    void exists_email_blank() throws Exception {
        mockMvc.perform(
                        get("/api/customers/exists")
                                .param("email", "")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("email 형식이 올바르지 않으면 400 반환")
    void exists_email_invalid_format() throws Exception {
        mockMvc.perform(
                        get("/api/customers/exists")
                                .param("email", "invalid-email")
                )
                .andExpect(status().isBadRequest());
    }
}
