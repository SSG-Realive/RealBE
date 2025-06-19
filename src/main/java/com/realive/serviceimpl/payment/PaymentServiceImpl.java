//package com.realive.serviceimpl.payment;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.realive.domain.common.enums.OrderStatus;
//import com.realive.domain.common.enums.PaymentStatus;
//import com.realive.domain.order.Order;
//import com.realive.domain.payment.Payment;
//import com.realive.dto.payment.TossPaymentApproveRequestDTO;
//import com.realive.dto.payment.TossPaymentApproveResponseDTO;
//import com.realive.repository.order.OrderRepository;
//import com.realive.repository.payment.PaymentRepository;
//import com.realive.service.payment.PaymentService;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.server.ResponseStatusException; // 필수 임포트
//import reactor.core.publisher.Mono; // 필수 임포트
//
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.util.Base64;
//import java.util.Objects;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class PaymentServiceImpl implements PaymentService {
//
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    private final PaymentRepository paymentRepository;
//    private final OrderRepository orderRepository;
//    private final WebClient webClient;
//    private final ObjectMapper objectMapper;
//
//    @Value("${toss.secret-key}")
//    private String tossSecretKey;
//
//    @Value("${toss.api-url}")
//    private String tossApiUrl;
//
//    @Override
//    @Transactional
//    public Payment approveTossPayment(TossPaymentApproveRequestDTO request) {
//        // 1. 토스페이먼츠 API 호출을 위한 인증 헤더 생성
//        // Corrected Line 56: encodedAuth 변수를 사용하여 authorization 생성
//        String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
//        String authorization = "Basic " + encodedAuth; // 'encodedBytes' 대신 'encodedAuth' 사용
//
//        TossPaymentApproveResponseDTO tossResponse;
//        try {
//            // 2. 토스페이먼츠 결제 승인 API 호출
//            tossResponse = webClient.post()
//                    .uri(tossApiUrl + "/v1/payments/confirm")
//                    .header(HttpHeaders.AUTHORIZATION, authorization)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(BodyInserters.fromValue(request))
//                    .retrieve()
//                    // 4xx 클라이언트 에러 처리 (Line 68, 70 관련)
//                    .onStatus(HttpStatus::is4xxClientError, clientResponse ->
//                            clientResponse.bodyToMono(String.class)
//                                    .flatMap(errorBody -> Mono.error(new ResponseStatusException(
//                                            clientResponse.statusCode(),
//                                            "Toss Payments Client Error: " + errorBody
//                                    ))) // Corrected: Mono.error() 사용 (Bad return type 해결)
//                    )
//                    // 5xx 서버 에러 처리 (Line 76, 78 관련)
//                    .onStatus(HttpStatus::is5xxServerError, clientResponse ->
//                            clientResponse.bodyToMono(String.class)
//                                    .flatMap(errorBody -> Mono.error(new ResponseStatusException(
//                                            clientResponse.statusCode(),
//                                            "Toss Payments Server Error: " + errorBody
//                                    ))) // Corrected: Mono.error() 사용 (Bad return type 해결)
//                    )
//                    .bodyToMono(TossPaymentApproveResponseDTO.class)
//                    .block(); // 비동기이지만 여기서는 동기적으로 블로킹하여 결과 대기
//        } catch (Exception e) {
//            logger.error("토스페이먼츠 API 호출 중 오류 발생: {}", e.getMessage(), e);
//            throw new RuntimeException("토스페이먼츠 API 호출 중 오류가 발생했습니다.", e);
//        }
//
//        // 3. 응답 유효성 검증
//        if (tossResponse == null ||
//                !Objects.equals(tossResponse.getOrderId(), request.getOrderId()) || // 요청 orderId와 응답 orderId 일치 확인
//                !Objects.equals(tossResponse.getTotalAmount(), request.getAmount()) || // 요청 금액과 응답 금액 일치 확인 (가장 중요!)
//                !"DONE".equals(tossResponse.getStatus())) { // 토스 응답의 상태가 "DONE"인지 확인
//            logger.error("토스페이먼츠 결제 승인 응답이 유효하지 않거나 실패 상태입니다. 요청: {}, 응답: {}", request, tossResponse);
//            throw new IllegalArgumentException("토스페이먼츠 결제 승인 응답이 유효하지 않거나 실패 상태입니다.");
//        }
//
//        // 4. 해당 주문(Order) 엔티티 조회 및 업데이트
//        Long ourOrderId;
//        try {
//            ourOrderId = Long.parseLong(request.getOrderId());
//        } catch (NumberFormatException e) {
//            logger.error("Invalid orderId format from Toss Payment: {}", request.getOrderId());
//            throw new IllegalArgumentException("주문 ID 형식이 올바르지 않습니다.", e);
//        }
//
//        Order order = orderRepository.findById(ourOrderId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다: " + ourOrderId));
//
//        // 주문 상태를 OrderStatus.PAYMENT_COMPLETED로 업데이트
//        order.setStatus(OrderStatus.PAYMENT_COMPLETED);
//        orderRepository.save(order); // 주문 상태 변경 저장
//
//        // 5. Payment 엔티티 생성 및 저장
//        Payment payment = Payment.builder()
//                .paymentKey(tossResponse.getPaymentKey())
//                .order(order)
//                .amount(tossResponse.getTotalAmount().intValue())
//                .balanceAmount(tossResponse.getTotalAmount().intValue())
//                .method(tossResponse.getMethod())
//                .status(mapTossStatusToPaymentStatus(tossResponse.getStatus())) // <-- 매핑 메서드 사용
//                .requestedAt(tossResponse.getRequestedAt() != null ? tossResponse.getRequestedAt() : LocalDateTime.now())
//                .approvedAt(tossResponse.getApprovedAt() != null ? tossResponse.getApprovedAt() : LocalDateTime.now())
//                .type(tossResponse.getType())
//                .customerKey(tossResponse.getCustomerKey())
//                .currency(tossResponse.getCurrency())
//                .lastTransactionKey(tossResponse.getLastTransactionKey())
//                .rawResponseData(this.convertResponseToJson(tossResponse)) // 'this.' 명시
//                .build();
//
//        return paymentRepository.save(payment);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Payment getPaymentByOrderId(Long orderId) {
//        return paymentRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 주문의 결제 정보를 찾을 수 없습니다. orderId: " + orderId));
//    }
//
//    /**
//     * TossPaymentApproveResponseDTO 객체를 JSON 문자열로 변환하는 유틸리티 메서드
//     */
//    private String convertResponseToJson(TossPaymentApproveResponseDTO response) {
//        try {
//            return objectMapper.writeValueAsString(response);
//        } catch (JsonProcessingException e) {
//            logger.error("Failed to convert TossPaymentApproveResponseDTO to JSON: {}", response, e);
//            return null; // 변환 실패 시 null 반환 또는 적절한 예외 처리
//        }
//    }
//
//    /**
//     * 토스페이먼츠 응답 상태 문자열을 우리 시스템의 PaymentStatus Enum으로 매핑합니다.
//     */
//    private PaymentStatus mapTossStatusToPaymentStatus(String tossStatus) {
//        return switch (tossStatus) {
//            case "READY" -> PaymentStatus.READY;
//            case "IN_PROGRESS" -> PaymentStatus.IN_PROGRESS;
//            case "WAITING_FOR_DEPOSIT" -> PaymentStatus.WAITING_FOR_DEPOSIT;
//            case "DONE" -> PaymentStatus.COMPLETED;
//            case "CANCELED" -> PaymentStatus.CANCELED;
//            case "PARTIAL_CANCELED" -> PaymentStatus.PARTIAL_CANCELED;
//            case "EXPIRED" -> PaymentStatus.FAILED; // 만료된 결제는 실패로 간주
//            case "ABORTED" -> PaymentStatus.CANCELED; // 중단된 결제는 취소로 간주
//            // 토스페이먼츠 API 문서에서 모든 가능한 상태 값을 확인하고 여기에 추가하세요.
//            default -> {
//                logger.warn("Unknown Toss Payment Status: {}", tossStatus);
//                yield PaymentStatus.FAILED; // 알 수 없는 상태는 실패로 처리하거나, 필요에 따라 다른 Enum 값으로 매핑
//            }
//        };
//    }
//}