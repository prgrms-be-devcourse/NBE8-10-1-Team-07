package com.back.global.initData;

import com.back.domain.customer.customer.entity.Customer;
import com.back.domain.customer.customer.repository.CustomerRepository;
import com.back.domain.order.order.entity.OrderItem;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.order.entity.Order;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.product.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
        };
    }

    @Transactional
    public void work1() {
        if (productService.count() > 0) return;

        productService.create("columbia", 3000, "columbia coffee beans.");
        productService.create("콜롬비아", 3500, "콜롬비아산 원두입니다.");
        productService.create("에티오피아", 4000, "에티오피아산 예가체프 원두입니다.");
    }

    @Transactional
    public void work2() {
        if (orderRepository.count() > 0) return;

        // 기초 데이터 준비
        List<Product> products = productRepository.findAll();
        Product p1 = products.get(0);
        Product p2 = products.get(1);
        Product p3 = products.get(2);

        Customer c1 = customerRepository.findByEmail("test1@example.com")
                .orElseGet(() -> customerRepository.save(new Customer("test1@example.com")));
        Customer c2 = customerRepository.findByEmail("test2@example.com")
                .orElseGet(() -> customerRepository.save(new Customer("test2@example.com")));

        String addr1 = "서울시 강남구 테헤란로 123";
        String code1 = "06123";
        String addr2 = "부산시 해운대구 우동 456";
        String code2 = "48045";

        // 생성할 주문 정보들을 리스트로 구성
        List<Order> ordersToSave = List.of(
                Order.create(c1, addr1, code1, List.of(OrderItem.create(p1, 1), OrderItem.create(p2, 2))),
                Order.create(c1, addr1, code1, List.of(OrderItem.create(p3, 1))),
                Order.create(c2, addr1, code1, List.of(OrderItem.create(p1, 1), OrderItem.create(p2, 2))),
                Order.create(c2, addr1, code1, List.of(OrderItem.create(p3, 1))),
                Order.create(c1, addr2, code2, List.of(OrderItem.create(p1, 2))),
                Order.create(c1, addr2, code2, List.of(OrderItem.create(p2, 1), OrderItem.create(p3, 3))),
                Order.create(c2, addr2, code2, List.of(OrderItem.create(p1, 2))),
                Order.create(c2, addr2, code2, List.of(OrderItem.create(p2, 1), OrderItem.create(p3, 3)))
        );

        // 루프를 돌며 3초 간격으로 저장
        for (int i = 0; i < ordersToSave.size(); i++) {
            orderRepository.save(ordersToSave.get(i));

            // 마지막 데이터가 아닐 때만 10초 대기
            if (i < ordersToSave.size() - 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}