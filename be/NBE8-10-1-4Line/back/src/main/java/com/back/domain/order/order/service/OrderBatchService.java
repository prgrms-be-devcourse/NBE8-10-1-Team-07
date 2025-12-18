package com.back.domain.order.order.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.order.util.OrderFileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderBatchService {
    private final OrderRepository orderRepository;
    private final OrderFileHandler orderFileHandler;

    public void processDailyOrderBatch() {
        // 상태 ORDERED인 내역 찾기
        LocalDateTime twoPmToday = LocalDate.now().atTime(14, 0);
        log.info("배치 조회 시작 - 기준 시간: {}, 상태: {}", twoPmToday, OrderStatus.ORDERED);
        List<Order> targetOrders = orderRepository.findAllByOrderStatusAndOrderTimeBefore(
                OrderStatus.ORDERED,
                twoPmToday
        );
        log.info("조회된 주문 건수: {}건", targetOrders.size());

        if (targetOrders.isEmpty()) {
            log.info("처리할 대상이 없어 배치를 종료합니다.");
            return;
        }

        // CSV 파일 저장
        orderFileHandler.createOrderCsv(targetOrders);
        List<Long> ids = targetOrders.stream().map(Order::getId).toList();
        orderRepository.updateStatusByIds(ids, OrderStatus.SHIPPING);
    }
}