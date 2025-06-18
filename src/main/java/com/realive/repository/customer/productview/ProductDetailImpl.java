package com.realive.repository.customer.productview;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.realive.domain.product.QCategory;
import com.realive.domain.product.QProduct;
import com.realive.domain.product.QProductImage;
import com.realive.domain.seller.QSeller;
import com.realive.dto.product.ProductResponseDTO;

import lombok.RequiredArgsConstructor;

// [Customer] 상품 상세 조회 Repository 구현체

@Repository
@RequiredArgsConstructor
public class ProductDetailImpl implements ProductDetail {
    
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductResponseDTO> findProductDetailById(Long id) {

        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;
        QCategory category = QCategory.category;
        QSeller seller = QSeller.seller;

        ProductResponseDTO dto = queryFactory
                .select(Projections.bean(ProductResponseDTO.class,
                        product.id.as("id"),
                        product.name.as("name"),
                        product.description.as("description"),
                        product.price.as("price"),
                        product.stock.as("stock"),
                        product.width.as("width"),
                        product.depth.as("depth"),
                        product.height.as("height"),
                        product.status.stringValue().as("status"),
                        product.active.as("active"),
                        productImage.url.as("mainImageUrl"),
                        category.name.as("categoryName"),
                        seller.name.as("sellerName")
                ))
                .from(product)
                .leftJoin(productImage)
                .on(productImage.product.eq(product)
                        .and(productImage.isThumbnail.isTrue()))
                .leftJoin(category).on(product.category.eq(category))
                .leftJoin(seller).on(product.seller.eq(seller))
                .where(product.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(dto);
    }
    
}
