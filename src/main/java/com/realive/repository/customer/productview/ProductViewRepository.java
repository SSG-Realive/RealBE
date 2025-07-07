package com.realive.repository.customer.productview;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.product.Product;

// [Customer] 상품 찾기 Repository
public interface ProductViewRepository extends JpaRepository<Product, Long>, ProductSearch, ProductDetail {

    // 상품ID로 상품 찾기
    Optional<Product> findById(Long id);

    // ✅ 카테고리별 인기 상품 (찜 많은 순)
    @Query("""
    SELECT p
    FROM Product p
    JOIN p.wishlists w
    WHERE p.category.id = :categoryId AND p.active = true
    GROUP BY p.id
    ORDER BY COUNT(w.id) DESC
""")
    List<Product> findPopularProductsByCategory(@Param("categoryId") Long categoryId);


}
