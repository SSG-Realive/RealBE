package com.realive.service.cart.crud;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.DeliveryType;
import com.realive.domain.common.enums.PaymentType;
import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.customer.CartItem;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.DeliveryPolicy;
import com.realive.domain.product.Product;
import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;
import com.realive.dto.order.PayRequestDTO; // PayRequestDTO 사용
import com.realive.dto.order.ProductQuantityDTO; // ProductQuantityDTO 사용
import com.realive.dto.product.ProductResponseDTO;
import com.realive.repository.cart.CartItemRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderDeliveryRepository; // 추가
import com.realive.repository.order.OrderItemRepository; // 추가
import com.realive.repository.order.OrderRepository; // 추가
import com.realive.repository.product.DeliveryPolicyRepository; // 추가
import com.realive.repository.product.ProductRepository;
import com.realive.repository.customer.productview.ProductViewRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional; // jakarta.transaction.Transactional 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductViewRepository productViewRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository; // 추가: 주문 생성을 위해 필요
    private final OrderItemRepository orderItemRepository; // 추가: 주문 항목 저장을 위해 필요
    private final OrderDeliveryRepository orderDeliveryRepository; // 추가: 배송 정보 저장을 위해 필요
    private final DeliveryPolicyRepository deliveryPolicyRepository; // 추가: 배송 정책 조회를 위해 필요

    // 장바구니 추가
    @Override
    @Transactional
    public CartItemResponseDTO addCartItem(Long customerId, CartItemAddRequestDTO requestDTO) {
        Long productId = requestDTO.getProductId();
        int quantityToAdd = requestDTO.getQuantity();

        // Customer 엔티티 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + customerId + "번 고객을 찾을 수 없습니다."));

        // Product 엔티티 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + productId + "번 상품을 찾을 수 없습니다."));

        if (product.getStock() < quantityToAdd) {
            throw new IllegalArgumentException("상품 ID " + productId + "번의 재고가 부족합니다.");
        }
        if (!product.isActive() || product.getStatus() == null) {
            throw new IllegalArgumentException("상품 ID " + productId + "번이 활성화되어 있지 않거나 구매할 수 없는 상태입니다.");
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByCustomer_IdAndProduct_Id(customerId, productId);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantityToAdd;
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("상품 ID " + productId + "번의 재고가 부족하여 수량을 추가할 수 없습니다.");
            }
            cartItem.setQuantity(newQuantity);
            log.info("기존 장바구니 항목 ID {}의 수량을 갱신합니다.", cartItem.getId());
        } else {
            cartItem = CartItem.builder()
                    .customer(customer)
                    .product(product)
                    .quantity(quantityToAdd)
                    .build();
            log.info("고객 ID {}와 상품 ID {}에 대한 새로운 장바구니 항목을 추가합니다. 수량: {}", customerId, productId, quantityToAdd);
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        ProductResponseDTO productDetailDto = productViewRepository.findProductDetailById(savedCartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("ID " + savedCartItem.getProduct().getId() + "번 상품의 상세 정보를 찾을 수 없습니다."));

        return CartItemResponseDTO.from(savedCartItem, productDetailDto);
    }

    //장바구니 수량 변경 + 수량 0될 시 삭제
    @Override
    @Transactional
    public CartItemResponseDTO updateCartItemQuantity(Long customerId, Long cartItemId, CartItemUpdateRequestDTO requestDTO) {
        int newQuantity = requestDTO.getQuantity();

        CartItem cartItem = cartItemRepository.findByIdAndCustomer_Id(cartItemId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + cartItemId + "번 장바구니 항목을 찾을 수 없거나 권한이 없습니다."));

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("수량이 0 이하가 되어 장바구니 항목 ID {}를 제거했습니다.", cartItemId);
            return null; // 장바구니 항목 제거 시 null 반환
        }

        Product product = productRepository.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("장바구니 항목과 연결된 상품을 찾을 수 없습니다."));

        if (product.getStock() < newQuantity) {
            throw new IllegalArgumentException("상품 ID " + product.getId() + "번의 재고가 부족하여 수량을 " + newQuantity + "(으)로 업데이트할 수 없습니다.");
        }
        if (!product.isActive() || product.getStatus() == null) {
            throw new IllegalArgumentException("상품 ID " + product.getId() + "번이 활성화되어 있지 않거나 구매할 수 없는 상태입니다.");
        }

        cartItem.setQuantity(newQuantity);
        log.info("장바구니 항목 ID {}의 수량을 {}로 업데이트했습니다.", cartItemId, newQuantity);

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        ProductResponseDTO productDetailDto = productViewRepository.findProductDetailById(updatedCartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("ID " + updatedCartItem.getProduct().getId() + "번 상품의 상세 정보를 찾을 수 없습니다."));

        return CartItemResponseDTO.from(updatedCartItem, productDetailDto);
    }

    // 장바구니에서 항목 제거
    @Override
    @Transactional
    public void removeCartItem(Long customerId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndCustomer_Id(cartItemId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + cartItemId + "번 장바구니 항목을 찾을 수 없거나 권한이 없습니다."));

        cartItemRepository.delete(cartItem);
        log.info("장바구니 항목 ID {}를 제거했습니다.", cartItemId);
    }

    // 장바구니 비우기
    @Override
    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomer_Id(customerId);
        log.info("고객 ID {}의 장바구니를 모두 삭제하였습니다.", customerId);
    }

    // 장바구니 다수 상품 결제 진행 로직 (OrderService에서 이동)
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
        // OrderService 대신 CartService에서 직접 주문 생성 로직 수행
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
                .status(DeliveryStatus.INIT)
                .startDate(LocalDateTime.now())
                .build();
        orderDeliveryRepository.save(orderDelivery);

        // 장바구니에서 결제된 항목들 삭제
        // (주의: payRequestDTO.getOrderItems()는 productId와 quantity만 포함하므로,
        // 실제 장바구니에서 해당 상품들을 삭제하려면 customerId와 productId를 기준으로 찾아야 합니다.)
        for (ProductQuantityDTO itemDTO : payRequestDTO.getOrderItems()) {
            cartItemRepository.findByCustomer_IdAndProduct_Id(customer.getId(), itemDTO.getProductId())
                    .ifPresent(cartItemRepository::delete);
        }

        log.info("장바구니 주문 성공 및 생성 완료: 주문 ID {}", order.getId());
        return order.getId();
    }

    // 결제 처리를 하는 시뮬레이션 메서드 (CartServiceImpl 내부로 복사)
    private boolean processWithPaymentGateway(Customer customer, int amount, PaymentType paymentType) {
        log.info("--- PG사(Payment Gateway) 결제 요청 시뮬레이션 ---");
        log.info("  고객 ID: {}", customer.getId());
        log.info("  결제 금액: {}원", amount);
        log.info("  결제 수단: {}", paymentType.getDescription());
        log.info("  PG사 결제 성공 (시뮬레이션)");
        return true;
    }
}