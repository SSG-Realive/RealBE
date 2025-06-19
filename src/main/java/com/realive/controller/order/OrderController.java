package com.realive.controller.order;

import com.realive.dto.order.*;
import com.realive.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.realive.dto.customer.member.MemberLoginDTO;

// JWT/OAuth2를 통한 사용자 인증이 구현되어 있다면,
// @AuthenticationPrincipal 또는 SecurityContextHolder를 통해 customerId를 가져올 수 있습니다.
@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;

    /**
     * **단일 상품 바로 구매 정보 조회**
     * GET /api/orders/direct-payment-info
     * @param productId 상품 ID
     * @param quantity 수량
     * @param userDetails 현재 인증된 사용자 정보
     * @return DirectPaymentInfoDTO
     */
    @GetMapping("/direct-payment-info")
    public ResponseEntity<DirectPaymentInfoDTO> getDirectPaymentInfo(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal MemberLoginDTO userDetails) {
        log.info("단일 상품 바로 구매 정보 조회 요청: productId={}, quantity={}", productId, quantity);
        DirectPaymentInfoDTO info = orderService.getDirectPaymentInfo(productId, quantity);
        return ResponseEntity.ok(info);
    }

    /**
     * **단일 상품 바로 구매 및 결제 처리**
     * POST /api/orders/direct-payment
     * @param payRequestDTO 결제 및 주문 생성 요청 DTO (productId, quantity 필드 필수)
     * @return 생성된 주문의 ID (Long)
     */
    @PostMapping("/direct-payment")
    public ResponseEntity<Long> processDirectPayment(
            @RequestBody PayRequestDTO payRequestDTO,
            @AuthenticationPrincipal MemberLoginDTO userDetails) {
        log.info("단일 상품 바로 구매 요청 수신: {}", payRequestDTO);
        // Set customerId from authentication
        payRequestDTO.setCustomerId(userDetails.getId());
        Long orderId = orderService.processDirectPayment(payRequestDTO);
        log.info("단일 상품 주문이 성공적으로 생성되었습니다. 주문 ID: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED); // 201 Created
    }

    /**
     * **장바구니 다수 상품 결제 처리**
     * POST /api/orders/cart-payment
     * @param payRequestDTO 결제 및 주문 생성 요청 DTO (orderItems 필드 필수)
     * @return 생성된 주문의 ID (Long)
     */
    @PostMapping("/cart-payment")
    public ResponseEntity<Long> processCartPayment(@RequestBody PayRequestDTO payRequestDTO) {
        log.info("장바구니 다수 상품 결제 요청 수신: {}", payRequestDTO);
        Long orderId = orderService.processCartPayment(payRequestDTO);
        log.info("장바구니 주문이 성공적으로 생성되었습니다. 주문 ID: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED); // 201 Created
    }


    /**
     * 특정 주문 상세 조회
     * GET /api/orders/{orderId}?customerId={customerId} (또는 인증 정보 사용)
     * @param orderId 조회할 주문 ID
     * @param customerId 주문 소유자 고객 ID (인증 정보에서 가져오는 것이 권장됨)
     * @return OrderResponseDTO
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long orderId,
                                                     @RequestParam Long customerId) { // 실제 앱에서는 @AuthenticationPrincipal CustomerDetails customerrId 등을 사용
        log.info("주문 상세 조회 요청 수신: 주문 ID {}, 고객 ID {}", orderId, customerId);
        OrderResponseDTO order = orderService.getOrder(orderId, customerId);
        return new ResponseEntity<>(order, HttpStatus.OK); // 200 OK
    }

    /**
     * 주문 목록 조회 (페이징 지원)
     * GET /api/orders?page=0&size=10&sort=orderedAt,desc
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 기준)
     * @return Page<OrderResponseDTO>
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getOrderList(
            @PageableDefault(sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("주문 목록 조회 요청 수신: 페이지 {}, 크기 {}, 정렬 {}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<OrderResponseDTO> orderList = orderService.getOrderList(pageable);
        return new ResponseEntity<>(orderList, HttpStatus.OK); // 200 OK
    }

    /**
     * 주문 취소
     * POST /api/orders/cancel
     * @param orderCancelRequestDTO 취소할 주문 정보 (orderId, customerId, reason)
     * @return 응답 없음 (204 No Content)
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelOrder(@RequestBody OrderCancelRequestDTO orderCancelRequestDTO) {
        log.info("주문 취소 요청 수신: {}", orderCancelRequestDTO);
        orderService.cancelOrder(orderCancelRequestDTO);
        log.info("주문 취소 처리 완료: 주문 ID {}", orderCancelRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    /**
     * 구매 확정
     * POST /api/orders/confirm
     * @param orderConfirmRequestDTO 구매 확정할 주문 정보 (orderId, customerId)
     * @return 응답 없음 (204 No Content)
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmOrder(@RequestBody OrderConfirmRequestDTO orderConfirmRequestDTO) {
        log.info("구매 확정 요청 수신: {}", orderConfirmRequestDTO);
        orderService.confirmOrder(orderConfirmRequestDTO);
        log.info("구매 확정 처리 완료: 주문 ID {}", orderConfirmRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    /**
     * 주문 삭제
     * DELETE /api/orders
     * @param orderDeleteRequestDTO 삭제할 주문 정보 (orderId, customerId)
     * @return 응답 없음 (204 No Content)
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteOrder(@RequestBody OrderDeleteRequestDTO orderDeleteRequestDTO) {
        log.info("주문 삭제 요청 수신: {}", orderDeleteRequestDTO);
        orderService.deleteOrder(orderDeleteRequestDTO);
        log.info("주문 삭제 처리 완료: 주문 ID {}", orderDeleteRequestDTO.getOrderId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }
}