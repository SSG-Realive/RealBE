package com.realive.serviceimpl.seller;

import com.realive.domain.order.Order;
import com.realive.repository.order.OrderRepository;
import com.realive.service.seller.SellerPayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SellerPayoutScheduler {

    private final SellerPayoutService sellerPayoutService;
    private final OrderRepository orderRepository;

    // 매주 월요일 새벽 1시
    @Scheduled(cron = "0 0 1 * * MON")
    public void generateWeeklyPayoutLogs() {
        // 지난 주 완료된 주문들 찾기
        LocalDate lastWeekStart = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6);

        List<Order> completedOrders = orderRepository.findCompletedOrdersBetween(lastWeekStart, lastWeekEnd);

        for (Order order : completedOrders) {
            sellerPayoutService.generatePayoutLogIfNotExists(order.getId());
        }
    }

    // 매일 새벽 2시
    @Scheduled(cron = "0 0 2 * * *")
    public void generateDailyPayoutLogs() {
        // 어제 완료된 주문들 처리
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<Order> completedOrders = orderRepository.findCompletedOrdersByDate(yesterday);

        for (Order order : completedOrders) {
            sellerPayoutService.generatePayoutLogIfNotExists(order.getId());
        }
    }
}