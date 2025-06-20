package com.realive.service.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.DeliveryType;
import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.common.enums.PaymentType;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import com.realive.dto.order.*;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderDeliveryRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.DeliveryPolicyRepository;
import com.realive.repository.product.ProductImageRepository;
import com.realive.repository.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final CustomerRepository customerRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;

    // 구매내역 조회
    @Override
    public OrderResponseDTO getOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 구매 내역입니다. (주문 ID: " + orderId + ", 고객 ID: " + customerId + ")"));

        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(order.getId());

        if (orderItems.isEmpty()) {
            throw new NoSuchElementException("주문 항목이 없습니다.");
        }

        List<Long> productIdsInOrder = orderItems.stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> thumbnailUrls = productImageRepository.findThumbnailUrlsByProductIds(productIdsInOrder, MediaType.IMAGE)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (String) arr[1]
                ));

        // DeliveryPolicyRepository에 findByProductIds가 없으므로 findAll 후 필터링
        Map<Long, DeliveryPolicy> deliveryPoliciesByProductId = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> policy.getProduct() != null && productIdsInOrder.contains(policy.getProduct().getId()))
                .collect(Collectors.toMap(policy -> policy.getProduct().getId(), Function.identity()));


        List<OrderItemResponseDTO> itemDTOs = new ArrayList<>();
        int totalDeliveryFeeForOrder = 0;
        List<Long> processedProductIdsForDelivery = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();

            String imageUrl = thumbnailUrls.getOrDefault(product.getId(), null);

            int itemDeliveryFee = 0;
            DeliveryPolicy deliveryPolicy = deliveryPoliciesByProductId.get(product.getId());

            if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송 && !processedProductIdsForDelivery.contains(product.getId())) {
                itemDeliveryFee = deliveryPolicy.getCost();
                totalDeliveryFeeForOrder += itemDeliveryFee;
                processedProductIdsForDelivery.add(product.getId());
            }

            itemDTOs.add(OrderItemResponseDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .imageUrl(imageUrl)
                    .build());
        }

        // OrderDelivery 정보 조회
        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);
        // 여기서 DELIVERY_PREPARING 대신 INIT으로 설정
        String currentDeliveryStatus = optionalOrderDelivery
                .map(delivery -> delivery.getStatus().getDescription())
                .orElse(DeliveryStatus.INIT.getDescription()); // <--- 이 부분 수정
        String paymentType = "CARD"; // 다른 결제수단은 없음

        return OrderResponseDTO.from(
                order,
                itemDTOs,
                totalDeliveryFeeForOrder,
                paymentType,
                currentDeliveryStatus
        );
    }

    // 구매 내역 리스트 조회
    @Override
    public Page<OrderResponseDTO> getOrderList(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllOrders(pageable);
        List<OrderResponseDTO> responseList = new ArrayList<>();

        List<Long> orderIds = orderPage.getContent().stream().map(Order::getId).collect(Collectors.toList());

        Map<Long, List<OrderItem>> orderItemsByOrderId = orderItemRepository.findByOrder_IdIn(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        List<Long> productIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(item -> item.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> thumbnailUrls = productImageRepository.findThumbnailUrlsByProductIds(productIds, MediaType.IMAGE)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (String) arr[1]
                ));

        // DeliveryPolicyRepository에 findByProductIds가 없으므로 findAll 후 필터링
        Map<Long, DeliveryPolicy> deliveryPoliciesByProductId = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> policy.getProduct() != null && productIds.contains(policy.getProduct().getId()))
                .collect(Collectors.toMap(policy -> policy.getProduct().getId(), Function.identity()));

        Map<Long, String> deliveryStatusByOrderId = orderDeliveryRepository.findByOrderIn(orderPage.getContent()).stream()
                .collect(Collectors.toMap(
                        delivery -> delivery.getOrder().getId(),
                        delivery -> delivery.getStatus().getDescription(),
                        (existing, replacement) -> existing
                ));

        for (Order order : orderPage.getContent()) {
            List<OrderItem> currentOrderItems = orderItemsByOrderId.getOrDefault(order.getId(), new ArrayList<>());
            List<OrderItemResponseDTO> itemDTOs = new ArrayList<>();
            int totalDeliveryFeeForOrder = 0;
            List<Long> processedProductIdsForOrderListDelivery = new ArrayList<>();

            for (OrderItem item : currentOrderItems) {
                Product product = item.getProduct();
                String imageUrl = thumbnailUrls.getOrDefault(product.getId(), null);

                int itemDeliveryFee = 0;
                DeliveryPolicy deliveryPolicy = deliveryPoliciesByProductId.get(product.getId());
                if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송 && !processedProductIdsForOrderListDelivery.contains(product.getId())) {
                    itemDeliveryFee = deliveryPolicy.getCost();
                    totalDeliveryFeeForOrder += itemDeliveryFee;
                    processedProductIdsForOrderListDelivery.add(product.getId());
                }

                itemDTOs.add(OrderItemResponseDTO.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .imageUrl(imageUrl)
                        .build());
            }

            // 여기서도 DeliveryStatus.DELIVERY_PREPARING 대신 INIT으로 설정
            String currentDeliveryStatus = deliveryStatusByOrderId.getOrDefault(order.getId(), DeliveryStatus.INIT.getDescription()); // <--- 이 부분 수정
            String paymentType = "CARD";

            OrderResponseDTO orderDTO = OrderResponseDTO.from(
                    order,
                    itemDTOs,
                    totalDeliveryFeeForOrder,
                    paymentType,
                    currentDeliveryStatus
            );
            responseList.add(orderDTO);
        }

        long totalElements = orderPage.getTotalElements();

        return new PageImpl<>(responseList, pageable, totalElements);
    }

    // 구매 내역 삭제
    @Override
    @Transactional
    public void deleteOrder(OrderDeleteRequestDTO orderDeleteRequestDTO) {
        Long orderId = orderDeleteRequestDTO.getOrderId();
        Long customerId = orderDeleteRequestDTO.getCustomerId();

        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 주문을 찾을 수 없습니다: 주문 ID " + orderId + ", 고객 ID " + customerId));

        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);

        if (optionalOrderDelivery.isPresent()) {
            DeliveryStatus deliveryStatus = optionalOrderDelivery.get().getStatus();
            // INIT 상태도 삭제 가능하도록 허용
            if (!(deliveryStatus == DeliveryStatus.DELIVERY_PREPARING || deliveryStatus == DeliveryStatus.INIT)) { // <--- 이 부분 수정
                throw new IllegalStateException(String.format("현재 배송 상태가 '%s'이므로 주문을 삭제할 수 없습니다. '%s' 또는 '%s' 상태의 주문만 삭제 가능합니다.", // <--- 이 부분 수정
                        deliveryStatus.getDescription(),
                        DeliveryStatus.DELIVERY_PREPARING.getDescription(),
                        DeliveryStatus.INIT.getDescription())); // <--- 이 부분 수정
            }
        }

        if (!(order.getStatus() == OrderStatus.PAYMENT_COMPLETED || order.getStatus() == OrderStatus.ORDER_RECEIVED || order.getStatus() == OrderStatus.INIT)) { // <--- 이 부분 수정
            throw new IllegalStateException(String.format("현재 주문 상태가 '%s'이므로 삭제할 수 없습니다. 삭제 가능한 상태: (%s, %s, %s)", // <--- 이 부분 수정
                    order.getStatus().getDescription(),
                    OrderStatus.PAYMENT_COMPLETED.getDescription(), OrderStatus.ORDER_RECEIVED.getDescription(), OrderStatus.INIT.getDescription())); // <--- 이 부분 수정
        }

        List<OrderItem> orderItemsToDelete = orderItemRepository.findByOrder_Id(order.getId());
        orderItemRepository.deleteAll(orderItemsToDelete);

        optionalOrderDelivery.ifPresent(orderDeliveryRepository::delete);

        orderRepository.delete(order);
        log.info("주문이 성공적으로 삭제되었습니다: 주문 ID {}", orderId);
    }

    // 구매 취소
    @Override
    @Transactional
    public void cancelOrder(OrderCancelRequestDTO orderCancelRequestDTO) {
        Long orderId = orderCancelRequestDTO.getOrderId();
        Long customerId = orderCancelRequestDTO.getCustomerId();
        String reason = orderCancelRequestDTO.getReason();

        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("취소하려는 주문을 찾을 수 없습니다 : 주문 ID " + orderId + ", 고객 ID " + customerId));

        Optional<OrderDelivery> optionalOrderDelivery = orderDeliveryRepository.findByOrder(order);

        if (optionalOrderDelivery.isPresent()) {
            DeliveryStatus deliveryStatus = optionalOrderDelivery.get().getStatus();
            // INIT 상태도 취소 가능하도록 허용
            if (!(deliveryStatus == DeliveryStatus.DELIVERY_PREPARING || deliveryStatus == DeliveryStatus.INIT)) { // <--- 이 부분 수정
                throw new IllegalStateException(String.format("현재 배송 상태가 '%s'이므로 주문을 취소할 수 없습니다. '%s' 또는 '%s' 상태의 주문만 취소 가능합니다.", // <--- 이 부분 수정
                        deliveryStatus.getDescription(),
                        DeliveryStatus.DELIVERY_PREPARING.getDescription(),
                        DeliveryStatus.INIT.getDescription())); // <--- 이 부분 수정
            }

            optionalOrderDelivery.get().setCompleteDate(LocalDateTime.now());
            orderDeliveryRepository.save(optionalOrderDelivery.get());
        }

        if (!(order.getStatus() == OrderStatus.PAYMENT_COMPLETED ||
                order.getStatus() == OrderStatus.ORDER_RECEIVED || order.getStatus() == OrderStatus.INIT)) { // <--- 이 부분 수정
            throw new IllegalStateException(String.format("현재 주문 상태가 '%s'이므로 취소할 수 없습니다. 취소 가능한 상태: (%s, %s, %s)", // <--- 이 부분 수정
                    order.getStatus().getDescription(),
                    OrderStatus.PAYMENT_COMPLETED.getDescription(), OrderStatus.ORDER_RECEIVED.getDescription(), OrderStatus.INIT.getDescription())); // <--- 이 부분 수정
        }

        order.setStatus(OrderStatus.PURCHASE_CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // TODO: 판매(product.stock) 수량 복구 로직 필요

        log.info("주문 상태가 '구매취소'로 변경되었습니다: 주문 ID {}", orderId);
        if (reason != null && !reason.isEmpty()) {
            log.info("구매취소 사유: {}", reason);
        }
    }

    // 구매 확정
    @Override
    @Transactional
    public void confirmOrder(OrderConfirmRequestDTO orderConfirmRequestDTO) {
        Long orderId = orderConfirmRequestDTO.getOrderId();
        Long customerId = orderConfirmRequestDTO.getCustomerId();

        Order order = orderRepository.findByCustomer_IdAndId(customerId, orderId)
                .orElseThrow(() -> new NoSuchElementException("구매확정 하려는 주문을 찾을 수 없습니다: 주문 ID " + orderId + ", 고객 ID " + customerId));

        OrderDelivery orderDelivery = orderDeliveryRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalStateException("배송 정보가 없는 주문은 구매 확정할 수 없습니다."));

        if (orderDelivery.getStatus() != DeliveryStatus.DELIVERY_COMPLETED) {
            throw new IllegalStateException(String.format("현재 배송 상태가 '%s'이므로 구매확정할 수 없습니다. 구매확정은 '%s' 상태의 주문만 가능합니다.",
                    orderDelivery.getStatus().getDescription(),
                    DeliveryStatus.DELIVERY_COMPLETED.getDescription()));
        }

        order.setStatus(OrderStatus.PURCHASE_CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("주문 상태가 '구매확정'으로 변경되었습니다: 주문 ID {}", orderId);
    }

    // 단일 상품 바로 구매 결제 진행 로직
    @Override
    @Transactional
    public Long processDirectPayment(PayRequestDTO payRequestDTO) {
        log.info("단일 상품 바로 구매 결제 처리 시작: {}", payRequestDTO);

        // **유효성 검증: 단일 상품 결제에 필요한 필드 확인**
        if (payRequestDTO.getProductId() == null || payRequestDTO.getQuantity() == null || payRequestDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("단일 상품 결제에는 productId와 quantity가 필수이며, 수량은 1개 이상이어야 합니다.");
        }
        if (payRequestDTO.getOrderItems() != null && !payRequestDTO.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("단일 상품 결제 시에는 orderItems를 포함할 수 없습니다.");
        }

        // 고객 정보 조회
        Customer customer = customerRepository.findById(payRequestDTO.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + payRequestDTO.getCustomerId()));

        // 배송 및 결제 정보 조회 (공통)
        String receiverName = payRequestDTO.getReceiverName();
        String phone = payRequestDTO.getPhone();
        String deliveryAddress = payRequestDTO.getDeliveryAddress();
        PaymentType paymentType = payRequestDTO.getPaymentMethod();

        if (receiverName == null || receiverName.isEmpty() ||
                phone == null || phone.isEmpty() ||
                deliveryAddress == null || deliveryAddress.isEmpty() ||
                paymentType == null) {
            throw new IllegalArgumentException("필수 배송 및 결제 정보가 누락되었습니다.");
        }

        List<OrderItem> orderItemsToSave = new ArrayList<>();
        int calculatedTotalProductPrice = 0;
        int totalDeliveryFee = 0;

        // 상품 정보 조회
        Product product = productRepository.findById(payRequestDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("결제하려는 상품을 찾을 수 없습니다: ID " + payRequestDTO.getProductId()));

        // DeliveryPolicyRepository 수정 권한이 없으므로 findAll 후 필터링하여 찾음
        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> policy.getProduct() != null && policy.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        // TODO: 재고 확인 및 감소 로직 추가
        // if (product.getStock() < payRequestDTO.getQuantity()) {
        //     throw new IllegalStateException("상품 재고가 부족합니다.");
        // }
        // product.decreaseStock(payRequestDTO.getQuantity());
        // productRepository.save(product); // 재고 감소 후 저장

        calculatedTotalProductPrice += product.getPrice() * payRequestDTO.getQuantity();

        if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송) {
            totalDeliveryFee += deliveryPolicy.getCost();
        }

        orderItemsToSave.add(OrderItem.builder()
                .product(product)
                .quantity(payRequestDTO.getQuantity())
                .price(product.getPrice()) // 주문 당시 상품 가격 저장
                .build());

        // 최종 결제 금액 계산
        int finalTotalPrice = calculatedTotalProductPrice + totalDeliveryFee;

        // Payment Gateway 연동 (시뮬레이션)
        boolean paymentSuccess = processWithPaymentGateway(customer, finalTotalPrice, paymentType);
        if (!paymentSuccess) {
            throw new IllegalStateException("결제 처리 중 오류가 발생했거나 결제가 실패했습니다.");
        }

        // ------------------ 결제 성공 후 DB에 주문 정보 저장 ------------------
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PAYMENT_COMPLETED) // 주문 상태는 결제 완료
                .totalPrice(finalTotalPrice)
                .deliveryAddress(deliveryAddress)
                .orderedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);

        for (OrderItem item : orderItemsToSave) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(orderItemsToSave); // 모든 주문 항목 한 번에 저장

        // OrderDelivery 상태를 INIT으로 설정
        OrderDelivery orderDelivery = OrderDelivery.builder()
                .order(order)
                .status(DeliveryStatus.INIT) // <--- 이 부분 수정: INIT으로 변경
                .startDate(LocalDateTime.now())
                .build();
        orderDeliveryRepository.save(orderDelivery);

        log.info("단일 상품 주문 성공 및 생성 완료: 주문 ID {}", order.getId());
        return order.getId();
    }

    // 장바구니 다수 상품 결제 진행 로직
    @Override
    @Transactional
    public Long processCartPayment(PayRequestDTO payRequestDTO) {
        log.info("장바구니 다수 상품 결제 처리 시작: {}", payRequestDTO);

        // **유효성 검증: 장바구니 결제에 필요한 필드 확인**
        if (payRequestDTO.getOrderItems() == null || payRequestDTO.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니 결제에는 orderItems가 필수이며, 비어있지 않아야 합니다.");
        }
        if (payRequestDTO.getProductId() != null || payRequestDTO.getQuantity() != null) {
            throw new IllegalArgumentException("장바구니 결제 시에는 productId와 quantity를 포함할 수 없습니다.");
        }

        // 고객 정보 조회
        Customer customer = customerRepository.findById(payRequestDTO.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + payRequestDTO.getCustomerId()));

        // 배송 및 결제 정보 조회 (공통)
        String receiverName = payRequestDTO.getReceiverName();
        String phone = payRequestDTO.getPhone();
        String deliveryAddress = payRequestDTO.getDeliveryAddress();
        PaymentType paymentType = payRequestDTO.getPaymentMethod();

        if (receiverName == null || receiverName.isEmpty() ||
                phone == null || phone.isEmpty() ||
                deliveryAddress == null || deliveryAddress.isEmpty() ||
                paymentType == null) {
            throw new IllegalArgumentException("필수 배송 및 결제 정보가 누락되었습니다.");
        }

        List<OrderItem> orderItemsToSave = new ArrayList<>();
        int calculatedTotalProductPrice = 0;
        int totalDeliveryFee = 0;
        List<Long> processedProductIdsForDeliveryCalculation = new ArrayList<>(); // 배송비 중복 방지

        // 요청된 모든 상품 ID 수집 및 상품 정보 일괄 조회
        List<Long> requestedProductIds = payRequestDTO.getOrderItems().stream()
                .map(ProductQuantityDTO::getProductId)
                .collect(Collectors.toList());
        Map<Long, Product> productsMap = productRepository.findAllById(requestedProductIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // DeliveryPolicyRepository 수정 권한이 없으므로 findAll 후 필터링
        Map<Long, DeliveryPolicy> deliveryPoliciesMap = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> policy.getProduct() != null && requestedProductIds.contains(policy.getProduct().getId()))
                .collect(Collectors.toMap(policy -> policy.getProduct().getId(), Function.identity()));

        for (ProductQuantityDTO itemDTO : payRequestDTO.getOrderItems()) {
            Product product = productsMap.get(itemDTO.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("결제하려는 상품을 찾을 수 없습니다: ID " + itemDTO.getProductId());
            }

            // TODO: 재고 확인 및 감소 로직 추가
            // if (product.getStock() < itemDTO.getQuantity()) {
            //     throw new IllegalStateException("상품 재고가 부족합니다: " + product.getName());
            // }
            // product.decreaseStock(itemDTO.getQuantity());
            // productRepository.save(product); // 재고 감소 후 저장

            calculatedTotalProductPrice += product.getPrice() * itemDTO.getQuantity();

            // 배송비 계산: 동일한 상품이 여러 번 요청되어도 배송비는 한 번만 부과 (단일 상품 배송 정책 가정)
            DeliveryPolicy deliveryPolicy = deliveryPoliciesMap.get(product.getId());
            if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송 && !processedProductIdsForDeliveryCalculation.contains(product.getId())) {
                totalDeliveryFee += deliveryPolicy.getCost();
                processedProductIdsForDeliveryCalculation.add(product.getId());
            }

            orderItemsToSave.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .price(product.getPrice()) // 주문 당시 상품 가격 저장
                    .build());
        }

        // 최종 결제 금액 계산
        int finalTotalPrice = calculatedTotalProductPrice + totalDeliveryFee;

        // Payment Gateway 연동 (시뮬레이션)
        boolean paymentSuccess = processWithPaymentGateway(customer, finalTotalPrice, paymentType);
        if (!paymentSuccess) {
            throw new IllegalStateException("결제 처리 중 오류가 발생했거나 결제가 실패했습니다.");
        }

        // ------------------ 결제 성공 후 DB에 주문 정보 저장 ------------------
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PAYMENT_COMPLETED) // 주문 상태는 결제 완료
                .totalPrice(finalTotalPrice)
                .deliveryAddress(deliveryAddress)
                .orderedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);

        for (OrderItem item : orderItemsToSave) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(orderItemsToSave); // 모든 주문 항목 한 번에 저장

        // OrderDelivery 상태를 INIT으로 설정
        OrderDelivery orderDelivery = OrderDelivery.builder()
                .order(order)
                .status(DeliveryStatus.INIT) // <--- 이 부분 수정: INIT으로 변경
                .startDate(LocalDateTime.now())
                .build();
        orderDeliveryRepository.save(orderDelivery);

        log.info("장바구니 주문 성공 및 생성 완료: 주문 ID {}", order.getId());
        return order.getId();
    }

    // 결제 처리를 하는 시뮬레이션 메서드
    private boolean processWithPaymentGateway(Customer customer, int amount, PaymentType paymentType) {
        log.info("--- PG사(Payment Gateway) 결제 요청 시뮬레이션 ---");
        log.info("  고객 ID: {}", customer.getId());
        log.info("  결제 금액: {}원", amount);
        log.info("  결제 수단: {}", paymentType.getDescription());
        log.info("  PG사 결제 성공 (시뮬레이션)");
        return true;
    }

    @Override
    public DirectPaymentInfoDTO getDirectPaymentInfo(Long productId, Integer quantity) {
        log.info("단일 상품 바로 구매 정보 조회: productId={}, quantity={}", productId, quantity);

        // 상품 정보 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: ID " + productId));

        // 배송 정책 조회
        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findAll().stream()
                .filter(policy -> policy.getProduct() != null && policy.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        // 배송비 계산
        int deliveryFee = 0;
        if (deliveryPolicy != null && deliveryPolicy.getType() == DeliveryType.유료배송) {
            deliveryFee = deliveryPolicy.getCost();
        }

        // 상품 이미지 URL 조회
        String imageUrl = productImageRepository.findThumbnailUrlsByProductIds(Collections.singletonList(productId), MediaType.IMAGE)
                .stream()
                .findFirst()
                .map(arr -> (String) arr[1])
                .orElse(null);

        // 총 상품 가격 계산
        int totalProductPrice = product.getPrice() * quantity;
        int totalPrice = totalProductPrice + deliveryFee;

        return DirectPaymentInfoDTO.builder()
                .productId(productId)
                .productName(product.getName())
                .quantity(quantity)
                .price(product.getPrice())
                .totalPrice(totalPrice)
                .deliveryFee(deliveryFee)
                .imageUrl(imageUrl)
                .build();
    }
}