package com.realive.repository.customer.productview;

import java.util.Collections;
import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

@Log4j2
@RequiredArgsConstructor
@Repository
public class ProductSearchImpl implements ProductSearch {

    private final JPAQueryFactory queryFactory;

    /**
     * 주어진 카테고리 ID와 그 하위 카테고리 ID 리스트를 조회하는 메서드
     * 자식 카테고리가 없으면 자기 자신 ID만 반환
     */
    public List<Long> findSubCategoryIdsIncludingSelf(Long categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }

        QCategory category = QCategory.category;

        List<Long> categoryIds = queryFactory
                .select(category.id)
                .from(category)
                .where(category.id.eq(categoryId)
                        .or(category.parent.id.eq(categoryId)))
                .fetch();

        // 자식 카테고리가 없으면 자기 자신만 포함되었는지 확인 후 보장
        if (!categoryIds.contains(categoryId)) {
            categoryIds.add(categoryId);
        }

        return categoryIds;
    }

    @Override
    public PageResponseDTO<ProductListDTO> search(PageRequestDTO requestDTO, Long categoryId) {

        QProduct product = QProduct.product;
        QCategory category = QCategory.category;
        QProductImage productImage = QProductImage.productImage;
        QSeller seller = QSeller.seller;

        BooleanBuilder builder = new BooleanBuilder();

        String keyword = requestDTO.getKeyword();
        String[] types = requestDTO.getType() != null ? requestDTO.getType().split("") : new String[]{};

        if (keyword != null && !keyword.isBlank()) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "T" -> keywordBuilder.or(product.name.containsIgnoreCase(keyword));
                    case "S" -> keywordBuilder.or(product.seller.name.containsIgnoreCase(keyword));
                    case "C" -> keywordBuilder.or(product.category.name.containsIgnoreCase(keyword));
                }
            }
            builder.and(keywordBuilder);
        }

        if (categoryId != null) {
            List<Long> categoryIds = findSubCategoryIdsIncludingSelf(categoryId);
            log.info("📂 포함된 카테고리 ID 목록: {}", categoryIds);
            builder.and(product.category.id.in(categoryIds));
        } else {
            // categoryId가 null이면 조건 없음 → 전체 상품 조회
            log.info("📂 전체 카테고리 대상 조회");
        }

        int offset = requestDTO.getOffset();
        int limit = requestDTO.getLimit();

        JPQLQuery<ProductListDTO> query = queryFactory
                .select(Projections.bean(ProductListDTO.class,
                        product.id.as("id"),
                        product.name.as("name"),
                        product.price.as("price"),
                        product.status.stringValue().as("status"),
                        product.active.as("isActive"),
                        productImage.url.as("imageThumbnailUrl"),
                        category.parent.name.as("parentCategoryName"),
                        seller.name.as("sellerName"),
                        seller.id.as("sellerId"),
                        category.name.as("categoryName"),
                        product.stock.as("stock")
                ))
                .from(product)
                .leftJoin(productImage)
                .on(productImage.product.eq(product)
                        .and(productImage.isThumbnail.isTrue()))
                .leftJoin(product.seller, seller)
                .leftJoin(product.category, category)
                .where(builder)
                .offset(offset)
                .limit(limit)
                .orderBy(product.id.desc());

        List<ProductListDTO> dtoList = query.fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return PageResponseDTO.<ProductListDTO>withAll()
                .pageRequestDTO(requestDTO)
                .dtoList(dtoList)
                .total(total != null ? total.intValue() : 0)
                .build();
    }
}
