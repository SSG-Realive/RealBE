package com.realive.repository.product;

import com.realive.domain.product.Product;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 상품 정보를 DB에서 조회/저장/삭제하는 JPA Repository
 */
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    /**
     * 판매자 ID로 상품 목록 조회
     * 
     * @param sellerId 판매자 ID
     * @return 해당 판매자의 상품 리스트
     */
    List<Product> findBySellerId(Long sellerId);

    // 오늘 등록된 신규 상품 수 조회 (BaseTimeEntity의 createdAt 필드 사용 가정)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdAt >= :startOfDay AND p.createdAt < :endOfDay")
    long countByCreatedAtBetween(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    List<Product> findAllByIdIn(List<Long> ids); // 여러 ID로 조회

    // 등록된 상품 수 (isActive = true)
    long countBySellerIdAndActiveTrue(Long sellerId);

    // 오늘 등록된 상품 수
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countTodayProductBySellerId(@Param("sellerId") Long sellerId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // 🚩 월별 상품 등록 통계
    @Query("SELECT YEAR(p.createdAt) as year, MONTH(p.createdAt) as month, COUNT(p) as count " +
           "FROM Product p " +
           "WHERE p.createdAt >= :startDate AND p.createdAt < :endDate " +
           "GROUP BY YEAR(p.createdAt), MONTH(p.createdAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyProductRegistrationStats(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    // 🚩 일별 상품 등록 통계
    @Query("SELECT DATE(p.createdAt) as date, COUNT(p) as count " +
           "FROM Product p " +
           "WHERE p.createdAt >= :startDate AND p.createdAt < :endDate " +
           "GROUP BY DATE(p.createdAt) " +
           "ORDER BY date")
    List<Object[]> getDailyProductRegistrationStats(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // 🚩 PESSIMISTIC LOCK 재고 차감용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Product findByIdForUpdate(@Param("productId") Long productId);

    // 관련 상품 추천
    List<Product> findTop6ByCategoryIdAndIdNotAndActiveTrue(Long categoryId, Long excludeProductId);

    /**
     * 특정 판매자 ID의 상품 중에서 랜덤으로 count개를 선택해 반환합니다.
     *
     * @param sellerId 판매자 ID
     * @param count    조회할 랜덤 상품 개수
     * @return 랜덤으로 선택된 상품 리스트
     */
    @Query(
      value = "SELECT * FROM products p WHERE p.seller_id = :sellerId ORDER BY RANDOM() LIMIT :count",
      nativeQuery = true
    )
    List<Product> findRandomProductsBySellerId(
      @Param("sellerId") Long sellerId,
      @Param("count") int count
    );

    /**
     * 재고가 특정 값보다 큰 상품 수 조회
     * 
     * @param stock 재고 기준값
     * @return 재고가 기준값보다 큰 상품 수
     */
    long countByStockGreaterThan(int stock);
}

