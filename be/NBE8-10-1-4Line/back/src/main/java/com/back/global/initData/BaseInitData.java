package com.back.global.initData;

import com.back.domain.customer.customer.entity.Customer;
import com.back.domain.customer.customer.repository.CustomerRepository;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderItem;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderItemRepository;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Profile("dev") // ✅ dev에서만 실행
public class BaseInitData {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Bean
    public org.springframework.boot.CommandLineRunner initData() {
        return args -> {
            // ✅ 중복 방지: 이미 있으면 아무것도 안 함
            String email = "test@test.com";
            if (customerRepository.existsByEmail(email)) return;

            // 1) 고객
            Customer c = new Customer();
            c.setEmail(email);
            customerRepository.save(c);

            // 2) 상품 2개
            Product p1 = new Product();
            p1.setName("콜롬비아 아메리카노");
            p1.setPrice(5000);
            p1.setDescription("고소한 맛");
            productRepository.save(p1);

            Product p2 = new Product();
            p2.setName("바닐라 라떼");
            p2.setPrice(6000);
            p2.setDescription("달달한 맛");
            productRepository.save(p2);

            // 3) 주문 2건 (각각 다른 상태/주소)
            Order o1 = new Order();
            o1.setCustomer(c);
            o1.setOrderTime(LocalDateTime.now().minusDays(1));
            o1.setTotalAmount(10000);
            o1.setOrderStatus(OrderStatus.SHIPPING);
            o1.setShippingAddress("부산시 주소1");
            o1.setShippingCode("12345");
            orderRepository.save(o1);

            Order o2 = new Order();
            o2.setCustomer(c);
            o2.setOrderTime(LocalDateTime.now());
            o2.setTotalAmount(6000);
            o2.setOrderStatus(OrderStatus.PAID);
            o2.setShippingAddress("경기도 주소1");
            o2.setShippingCode("67890");
            orderRepository.save(o2);

            // 4) 주문아이템
            OrderItem oi1 = new OrderItem();
            oi1.setOrder(o1);
            oi1.setProduct(p1);
            oi1.setQuantity(2);
            orderItemRepository.save(oi1);

            OrderItem oi2 = new OrderItem();
            oi2.setOrder(o2);
            oi2.setProduct(p2);
            oi2.setQuantity(1);
            orderItemRepository.save(oi2);
        };
    }
}
