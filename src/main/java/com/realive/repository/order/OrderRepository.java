package com.realive.repository.order;

import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 고객 ID와 주문 ID로 단건 조회
    Optional<Order> findByCustomerIdAndId(Long customerId, Long id);

    //주문 페이징처리된것
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    // 관리자 전체 주문 목록 조회 (Customer 정보 포함)
    @Query(value = """
            SELECT o FROM Order o
            JOIN FETCH o.customer
            ORDER BY o.orderedAt DESC
            """,
            countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllOrders(Pageable pageable);

    // 판매자 기준 진행 중인 주문 수 조회
@Query("""
    SELECT COUNT(DISTINCT oi.order)
    FROM OrderItem oi
    JOIN oi.product p
    WHERE p.seller.id = :sellerId
    AND oi.order.status IN :statuses
""")
long countInProgressOrders(@Param("sellerId") Long sellerId, @Param("statuses") Collection<OrderStatus> statuses);
    // 특정 Customer ID에 해당하는 모든 주문을 조회합니다.
    List<Order> findAllByCustomerId(Long customerId);

    // 스케줄러용: 특정 기간에 완료된 주문들 조회
    @Query("""
        SELECT o FROM Order o
        WHERE o.status = 'COMPLETED'
        AND DATE(o.orderedAt) BETWEEN :startDate AND :endDate
        ORDER BY o.orderedAt DESC
    """)
    List<Order> findCompletedOrdersBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 스케줄러용: 특정 날짜에 완료된 주문들 조회
    @Query("""
        SELECT o FROM Order o
        WHERE o.status = 'COMPLETED'
        AND DATE(o.orderedAt) = :date
        ORDER BY o.orderedAt DESC
    """)
    List<Order> findCompletedOrdersByDate(@Param("date") LocalDate date);

    // 스케줄러용: 특정 기간에 배송 완료된 주문들 조회 (배송 완료 상태)
    @Query("""
        SELECT o FROM Order o
        WHERE o.status = 'DELIVERED'
        AND DATE(o.orderedAt) BETWEEN :startDate AND :endDate
        ORDER BY o.orderedAt DESC
    """)
    List<Order> findDeliveredOrdersBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ 추가할 메서드: 판매자별 주문 상세 조회
    // ❌ 현재 쿼리에 문제가 있음 - JOIN FETCH 추가 필요
    @Query("""
    SELECT o FROM Order o
    JOIN FETCH o.orderItems oi
    JOIN FETCH o.customer
    JOIN FETCH oi.product p
    JOIN FETCH p.seller
    WHERE p.seller.id = :sellerId   
    AND o.id = :orderId
""")
    Optional<Order> findBySellerIdAndOrderId(@Param("sellerId") Long sellerId, @Param("orderId") Long orderId);

}