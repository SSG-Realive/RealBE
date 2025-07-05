package com.realive.repository.logs;

import com.realive.domain.logs.PayoutLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PayoutLogRepository extends JpaRepository<PayoutLog, Integer> {

    Optional<PayoutLog> findById(Integer id);

    @Query("select pol.id, pol.sellerId, pol.periodStart, pol.periodEnd, pol.totalSales, pol.totalCommission," +
            " pol.payoutAmount, pol.processedAt " +
            " from PayoutLog pol ")
    Page<Object[]> list1(Pageable pageable);

    // 특정 날짜에 처리된 총 정산 금액
    @Query("SELECT SUM(pl.payoutAmount) FROM PayoutLog pl WHERE DATE(pl.processedAt) = :date")
    Integer sumPayoutAmountByProcessedDate(LocalDate date);

    // 특정 정산 기간에 포함된 모든 정산 내역
    @Query("SELECT pl FROM PayoutLog pl WHERE :date BETWEEN pl.periodStart AND pl.periodEnd")
    List<PayoutLog> findByDateInPeriod(LocalDate date);

    // 특정 판매자의 정산 내역
    List<PayoutLog> findBySellerId(Integer sellerId);

    // 특정 기간 동안 처리된(processedAt 기준) 정산 내역 조회
    List<PayoutLog> findByProcessedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    // === 관리자 대시보드용 집계 메서드들 ===
    
    /**
     * 특정 날짜에 처리된 정산 건수 조회
     */
    @Query("SELECT COUNT(pl) FROM PayoutLog pl WHERE DATE(pl.processedAt) = :date")
    Long countPayoutsByDate(@Param("date") LocalDate date);

    /**
     * 특정 기간에 처리된 정산 건수 조회
     */
    @Query("SELECT COUNT(pl) FROM PayoutLog pl WHERE DATE(pl.processedAt) BETWEEN :startDate AND :endDate")
    Long countPayoutsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 날짜/시간 범위에 처리된 정산 건수 조회 (시간 포함)
     */
    @Query("SELECT COUNT(pl) FROM PayoutLog pl WHERE pl.processedAt BETWEEN :startDateTime AND :endDateTime")
    Long countPayoutsByDateTime(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 특정 날짜에 처리된 총 정산 금액 조회
     */
    @Query("SELECT COALESCE(SUM(pl.payoutAmount), 0) FROM PayoutLog pl WHERE DATE(pl.processedAt) = :date")
    Long sumPayoutAmountByDate(@Param("date") LocalDate date);

    /**
     * 특정 기간에 처리된 총 정산 금액 조회
     */
    @Query("SELECT COALESCE(SUM(pl.payoutAmount), 0) FROM PayoutLog pl WHERE DATE(pl.processedAt) BETWEEN :startDate AND :endDate")
    Long sumPayoutAmountByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 날짜/시간 범위에 처리된 총 정산 금액 조회 (시간 포함)
     */
    @Query("SELECT COALESCE(SUM(pl.payoutAmount), 0) FROM PayoutLog pl WHERE pl.processedAt BETWEEN :startDateTime AND :endDateTime")
    Long sumPayoutAmountByDateTime(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 매출 추이 그래프용 - 기간별 일일 매출 합계 조회
     */
    @Query("SELECT DATE(pl.processedAt) as date, COALESCE(SUM(pl.payoutAmount), 0) as totalAmount " +
           "FROM PayoutLog pl " +
           "WHERE DATE(pl.processedAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(pl.processedAt) " +
           "ORDER BY DATE(pl.processedAt)")
    List<Object[]> getDailyPayoutSummary(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 중복 로그 방지를 위한 체크
     */
     boolean existsBySellerIdAndPeriodStartAndPeriodEnd(Integer sellerId, LocalDate periodStart, LocalDate periodEnd);
     
    /**
     * 특정 판매자의 특정 기간 PayoutLog 조회
     */
     Optional<PayoutLog> findBySellerIdAndPeriodStartAndPeriodEnd(Integer sellerId, LocalDate periodStart, LocalDate periodEnd);

    /**
     * 특정 판매자의 기간별 정산 내역 조회
     */
    @Query("SELECT pl FROM PayoutLog pl WHERE pl.sellerId = :sellerId AND pl.periodStart <= :to AND pl.periodEnd >= :from")
    List<PayoutLog> findBySellerIdAndPeriodRange(@Param("sellerId") Integer sellerId, @Param("from") LocalDate from, @Param("to") LocalDate to);

}
