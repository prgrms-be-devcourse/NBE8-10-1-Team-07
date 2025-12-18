package com.back.domain.order.order.scheduler;

import com.back.domain.order.order.service.OrderBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderBatchScheduler {
    private final OrderBatchService orderBatchService;

    // @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul") // test
    @Scheduled(cron = "0 0 14 * * *", zone = "Asia/Seoul")
    public void runOrderBatch() {
        orderBatchService.processDailyOrderBatch();
    }
}