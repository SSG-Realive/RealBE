package com.realive.service.order;

import com.realive.dto.order.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    // 단일 주문 상세 조회
    OrderResponseDTO getOrder(Long orderId, Long customerId);

    // 주문 목록 조회 (페이징 포함)
    Page<OrderResponseDTO> getOrderList(Pageable pageable);

    // 구매내역 삭제
    void deleteOrder(OrderDeleteRequestDTO orderDeleteRequestDTO);

    // 구매 취소
    void cancelOrder(OrderCancelRequestDTO orderCancelRequestDTO);

    // 구매 확정
    void confirmOrder(OrderConfirmRequestDTO orderConfirmRequestDTO);

    // 단일 상품 바로 구매 결제 진행 및 구매내역 생성
    Long processDirectPayment(PayRequestDTO payRequestDTO); // 또는 DirectPayRequestDTO 사용 (선택 사항)

    /**
     * 단일 상품 바로 구매 정보 조회
     * @param productId 상품 ID
     * @param quantity 수량
     * @return DirectPaymentInfoDTO
     */
    DirectPaymentInfoDTO getDirectPaymentInfo(Long productId, Integer quantity);

    // 장바구니 다수 상품 결제 진행 및 구매내역 생성
    Long processCartPayment(PayRequestDTO payRequestDTO);
}