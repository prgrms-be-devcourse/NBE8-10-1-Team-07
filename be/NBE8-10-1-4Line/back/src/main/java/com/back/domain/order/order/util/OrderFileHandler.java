package com.back.domain.order.order.util;

import com.back.domain.order.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j // log 객체 자동 생성
public class OrderFileHandler {

    public void createOrderCsv(List<Order> orders) {
        Map<String, List<Order>> groupedOrders = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCustomer().getEmail()));

        LocalDateTime now = LocalDateTime.now();
        String dirPath = String.format("outputs/%d/%02d/", now.getYear(), now.getMonthValue());
        String fileName = String.format("order_report_%s.csv", now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));

        try {
            Files.createDirectories(Paths.get(dirPath));
            File file = new File(dirPath + fileName);

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "MS949"))) {

                writer.println("No,이메일,주소,우편번호,총결제금액,주문상품상세,최종주문시간");

                int no = 1;
                for (String email : groupedOrders.keySet()) {
                    List<Order> userOrders = groupedOrders.get(email);

                    // 그룹화된 데이터 합산
                    int totalAmount = userOrders.stream().mapToInt(Order::getTotalAmount).sum();
                    String shippingAddress = userOrders.getFirst().getShippingAddress();
                    String shippingCode = userOrders.getFirst().getShippingCode();
                    LocalDateTime lastOrderTime = userOrders.stream()
                            .map(Order::getOrderTime)
                            .max(LocalDateTime::compareTo).orElse(now);

                    // 상품 정보 합치기 (예: "columbia(1개) | 에티오피아(3개)")
                    String productSummary = userOrders.stream()
                            .flatMap(o -> o.getOrderItems().stream())
                            .map(item -> String.format("%s(%d개)", item.getProduct().getName(), item.getQuantity()))
                            .collect(Collectors.joining(" | "));

                    writer.printf("%d,\"%s\",\"%s\",\"%s\",%d,\"%s\",%s%n",
                            no++,
                            email,
                            shippingAddress,
                            shippingCode,
                            totalAmount,
                            productSummary,
                            lastOrderTime
                    );
                }
                log.info("CSV 생성 완료: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("CSV 파일 생성 중 에러", e);
            throw new RuntimeException(e);
        }
    }
}