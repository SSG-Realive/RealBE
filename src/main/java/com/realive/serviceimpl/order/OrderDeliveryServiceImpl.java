package com.realive.serviceimpl.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.Product;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.SellerOrderDeliveryRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.order.OrderDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDeliveryServiceImpl implements OrderDeliveryService {

    private final SellerOrderDeliveryRepository sellerOrderDeliveryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void updateDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto) {
        OrderDelivery delivery = sellerOrderDeliveryRepository
                .findByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        DeliveryStatus currentStatus = delivery.getStatus();
        DeliveryStatus newStatus = dto.getDeliveryStatus();

        boolean validTransition =
                (currentStatus == null && newStatus == DeliveryStatus.DELIVERY_PREPARING) || // 처음 PREPARING 으로 변경
                (currentStatus == DeliveryStatus.DELIVERY_PREPARING && newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS) ||
                (currentStatus == DeliveryStatus.DELIVERY_IN_PROGRESS && newStatus == DeliveryStatus.DELIVERY_COMPLETED);

        if (!validTransition) {
            throw new IllegalStateException("유효하지 않은 배송 상태 전이입니다.");
        }

        //배송 준비되면 stock 차감 로직 
        if (newStatus == DeliveryStatus.DELIVERY_PREPARING && currentStatus != DeliveryStatus.DELIVERY_PREPARING) {
            
            Long orderIdForItems = delivery.getOrder().getId();

            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderIdForItems);


            for (OrderItem item : orderItems) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId());

            if (product.getStock() < item.getQuantity()) {
                throw new IllegalStateException("재고가 부족하여 배송 준비 상태로 변경할 수 없습니다." + product.getName());
            }

            product.setStock(product.getStock() - item.getQuantity());
        }
    }
        // 배송중으로 변경 시 송장번호, 배송사 설정
        if (newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS) {
            if (dto.getTrackingNumber() != null) {
                delivery.setTrackingNumber(dto.getTrackingNumber());
            }
            if (dto.getCarrier() != null) {
                delivery.setCarrier(dto.getCarrier());
            }
        }
          // 상태 업데이트
        delivery.setStatus(newStatus);

        // 배송중 시작일 설정
        if (newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS && delivery.getStartDate() == null) {
            delivery.setStartDate(LocalDateTime.now());
        }

        // 배송완료 완료일 설정 + 🚩 isActive 처리 추가
        if (newStatus == DeliveryStatus.DELIVERY_COMPLETED && delivery.getCompleteDate() == null) {
            delivery.setCompleteDate(LocalDateTime.now());
        }

        Long orderIdForItems = delivery.getOrder().getId();
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderIdForItems);

        for (OrderItem item : orderItems) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId());

            // 🚩 재고가 0 인 경우에만 isActive = false 처리
            if (product.getStock() == 0 && product.isActive()) {
                product.setActive(false);
                log.info("Product {} 비활성화 처리됨", product.getId());
            }
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDeliveryResponseDTO> getDeliveriesBySeller(Long sellerId) {
        List<OrderDelivery> deliveries = sellerOrderDeliveryRepository.findAllBySellerId(sellerId);

        return deliveries.stream()
                .map(d -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(d.getOrder().getId());
                    String productName = orderItems.isEmpty() ? "상품 없음" : orderItems.get(0).getProduct().getName();

                    return OrderDeliveryResponseDTO.builder()
                            .orderId(d.getOrder().getId())
                            .productName(productName)
                            .buyerId(d.getOrder().getCustomer().getId())
                            .deliveryStatus(d.getStatus())
                            .startDate(d.getStartDate())
                            .completeDate(d.getCompleteDate())
                            .trackingNumber(d.getTrackingNumber())
                            .carrier(d.getCarrier())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDeliveryResponseDTO getDeliveryByOrderId(Long sellerId, Long orderId) {
        OrderDelivery delivery = sellerOrderDeliveryRepository
                .findByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문에 상품이 없습니다.");
        }

        if (!orderItems.get(0).getProduct().getSeller().getId().equals(sellerId)) {
            throw new SecurityException("자신의 주문이 아닙니다.");
        }

        String productName = orderItems.get(0).getProduct().getName();

        return OrderDeliveryResponseDTO.builder()
                .orderId(delivery.getOrder().getId())
                .productName(productName)
                .buyerId(delivery.getOrder().getCustomer().getId())
                .deliveryStatus(delivery.getStatus())
                .startDate(delivery.getStartDate())
                .completeDate(delivery.getCompleteDate())
                .trackingNumber(delivery.getTrackingNumber())
                .carrier(delivery.getCarrier())
                .build();
    }
}
