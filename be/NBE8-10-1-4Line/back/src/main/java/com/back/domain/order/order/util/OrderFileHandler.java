package com.back.domain.order.order.util;

import com.back.domain.order.order.dto.OrderDto;
import com.back.domain.order.order.dto.OrderItemDto;
import com.back.domain.order.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
@Slf4j
public class OrderFileHandler {

    public void createOrderCsv(List<Order> orders) {
        // 이메일로 그룹핑 -> 주소로 재그룹핑
        Map<String, Map<String, List<Order>>> groupedOrders = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCustomer().getEmail(),
                        Collectors.groupingBy(Order::getShippingAddress)));

        // outputs에 저장 -> 이후 필요 시 수정 요청 요망
        LocalDateTime now = LocalDateTime.now();
        String dirPath = String.format("outputs/%d/%02d/", now.getYear(), now.getMonthValue());
        String fileName = String.format("order_report_%s.csv", now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));

        try {
            Files.createDirectories(Paths.get(dirPath));
            File file = new File(dirPath + fileName);

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "MS949"))) {

                writer.println("No,주문ID,이메일,주소,우편번호,상품명,수량,단가,소계,주문시간");

                int rowNo = 1;

                for (Map.Entry<String, Map<String, List<Order>>> emailEntry : groupedOrders.entrySet()) {
                    String email = emailEntry.getKey();
                    Map<String, List<Order>> addressMap = emailEntry.getValue();

                    for (Map.Entry<String, List<Order>> addressEntry : addressMap.entrySet()) {
                        String address = addressEntry.getKey();

                        // 주소 그룹 내에서 주문 시간순 정렬
                        List<Order> sortedOrders = addressEntry.getValue().stream()
                                .sorted(Comparator.comparing(Order::getOrderTime))
                                .toList();

                        for (Order order : sortedOrders) {
                            OrderDto orderDto = new OrderDto(order); // DTO로 변환해 사용

                            for (OrderItemDto itemDto : orderDto.orderItems()) {
                                writer.printf("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%s%n",
                                        rowNo++,
                                        orderDto.id(),
                                        email,
                                        address,
                                        orderDto.shippingCode(),
                                        itemDto.productName(),
                                        itemDto.quantity(),
                                        itemDto.pricePerItem(),
                                        itemDto.subTotal(),
                                        orderDto.orderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                );
                            }
                        }
                    }
                }
                log.info("CSV 생성 완료: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("CSV 파일 생성 중 에러 발생", e);
            throw new RuntimeException("배치 파일 생성 실패", e);
        }
    }
}