package com.realive.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.Product;
import com.realive.domain.seller.Seller;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.repository.order.OrderDeliveryRepository;
import com.realive.repository.order.SellerOrderDeliveryRepository;
import com.realive.serviceimpl.order.OrderDeliveryServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderDeliveryServiceTest {

    @Mock
    private OrderDeliveryRepository orderDeliveryRepository;

    @Mock
    private SellerOrderDeliveryRepository sellerOrderDeliveryRepository;

    @InjectMocks
    private OrderDeliveryServiceImpl orderDeliveryService;

    private OrderDelivery orderDelivery;
    private DeliveryStatusUpdateDTO statusUpdateDTO;
    private Order order;
    private Product product;
    private Seller seller;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        // 판매자 설정
        seller = Seller.builder()
                .id(1L)
                .name("테스트 판매자")
                .build();

        // 상품 설정
        product = Product.builder()
                .id(1L)
                .name("테스트 상품")
                .seller(seller)
                .build();

        // 주문 항목 설정
        orderItem = OrderItem.builder()
                .id(1L)
                .product(product)
                .build();

        // 주문 설정
        order = Order.builder()
                .id(1L)
                .build();
        order.setOrderItems(new ArrayList<>());
        order.getOrderItems().add(orderItem);

        // 배송 설정
        orderDelivery = OrderDelivery.builder()
                .id(1L)
                .order(order)
                .status(DeliveryStatus.DELIVERY_PREPARING)
                .build();

        // 상태 업데이트 DTO 설정
        statusUpdateDTO = new DeliveryStatusUpdateDTO();
        statusUpdateDTO.setDeliveryStatus(DeliveryStatus.DELIVERY_IN_PROGRESS);
        statusUpdateDTO.setTrackingNumber("123456789");
        statusUpdateDTO.setCarrier("CJ택배");
    }

    @Test
    @DisplayName("배송 상태 업데이트 테스트")
    void updateDeliveryStatus() {
        // given
        Long sellerId = 1L;
        Long orderId = 1L;
        
        when(sellerOrderDeliveryRepository.findByOrderIdAndSellerId(orderId, sellerId))
            .thenReturn(Optional.of(orderDelivery));

        // when
        orderDeliveryService.updateDeliveryStatus(sellerId, orderId, statusUpdateDTO);

        // then
        verify(sellerOrderDeliveryRepository).findByOrderIdAndSellerId(orderId, sellerId);
        assertEquals(DeliveryStatus.DELIVERY_IN_PROGRESS, orderDelivery.getStatus());
        assertEquals("123456789", orderDelivery.getTrackingNumber());
        assertEquals("CJ택배", orderDelivery.getCarrier());
    }

    @Test
    @DisplayName("존재하지 않는 배송 정보 업데이트 시도 시 예외 발생")
    void updateDeliveryStatusWithNonExistentDelivery() {
        // given
        Long sellerId = 1L;
        Long orderId = 999L;
        
        when(sellerOrderDeliveryRepository.findByOrderIdAndSellerId(orderId, sellerId))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            orderDeliveryService.updateDeliveryStatus(sellerId, orderId, statusUpdateDTO);
        });
        
        verify(sellerOrderDeliveryRepository).findByOrderIdAndSellerId(orderId, sellerId);
    }
}