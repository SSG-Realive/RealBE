package com.realive.service.review.crud;

import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.review.SellerReviewImage;
import com.realive.domain.seller.Seller;
import com.realive.domain.review.SellerReview;
import com.realive.dto.review.ReviewCreateRequestDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.dto.review.ReviewUpdateRequestDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.review.crud.ReviewCRUDRepository;
import com.realive.repository.review.crud.SellerReviewImageRepository;
import com.realive.repository.seller.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewCRUDServiceImpl implements ReviewCRUDService {
    private final ReviewCRUDRepository reviewRepository;
    private final SellerReviewImageRepository imageRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateRequestDTO requestDTO, Long customerId) {
<<<<<<< HEAD
        // 1. 중복 리뷰 확인
        reviewRepository.findByOrderIdAndCustomerIdAndSellerId(
                        requestDTO.getOrderId(), customerId, requestDTO.getSellerId())
=======
        // 이미 해당 주문, 고객, 판매자 조합으로 리뷰가 있는지 확인
        reviewRepository.findByOrderIdAndCustomerIdAndSellerId(requestDTO.getOrderId(), customerId, requestDTO.getSellerId())
>>>>>>> dev
                .ifPresent(review -> {
                    throw new IllegalStateException("A review for this order and seller by this customer already exists.");
                });

        // 2. 엔티티 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));
        Order order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다.: " + requestDTO.getOrderId()));
        Seller seller = sellerRepository.findById(requestDTO.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 판매자입니다.: " + requestDTO.getSellerId()));

        // 3. 리뷰 생성
        SellerReview review = SellerReview.builder()
                .customer(customer)
                .order(order)
                .seller(seller)
                .rating(requestDTO.getRating().intValue())  // 🔧 int 변환
                .content(requestDTO.getContent())
                .build();

        // 4. 저장
        SellerReview savedReview = reviewRepository.save(review);

<<<<<<< HEAD
        // 5. 이미지 저장
        List<String> imageUrls = saveImages(savedReview, requestDTO.getImageUrls());
=======
        // 리뷰 이미지 처리: DTO의 이미지 URL을 바탕으로 SellerReviewImage 엔티티를 생성하고 저장합니다.
        List<String> savedImageUrls = List.of();
        if (requestDTO.getImageUrls() != null && !requestDTO.getImageUrls().isEmpty()) {
            List<SellerReviewImage> reviewImagesToSave = requestDTO.getImageUrls().stream()
                    .map(imageUrl -> {
                        boolean isThumbnail = requestDTO.getImageUrls().indexOf(imageUrl) == 0; // 첫 번째 이미지를 썸네일로 설정
                        return SellerReviewImage.builder()
                                .review(savedReview)
                                .imageUrl(imageUrl)
                                .thumbnail(isThumbnail)
                                .build();
                    })
                    .collect(Collectors.toList());
            imageRepository.saveAll(reviewImagesToSave);
            log.info("createReview - 리뷰 이미지 DB 저장 완료: reviewId={}, 이미지 수: {}", savedReview.getId(), reviewImagesToSave.size());

            savedImageUrls = imageRepository.findByReviewId(savedReview.getId())
                    .stream()
                    .map(SellerReviewImage::getImageUrl)
                    .collect(Collectors.toList());
        }

        String productName = getProductNameForReview(order, seller);
>>>>>>> dev

        // 6. DTO 변환
        return ReviewResponseDTO.builder()
                .reviewId(savedReview.getId())
                .orderId(savedReview.getOrder().getId())
                .customerId(savedReview.getCustomer().getId())
                .sellerId(savedReview.getSeller().getId())
                .productName(null)
                .rating(savedReview.getRating())
                .content(savedReview.getContent())
                .imageUrls(imageUrls)
                .createdAt(savedReview.getCreatedAt())
                .isHidden(savedReview.isHidden()) // 🔧 수정
                .build();
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateRequestDTO requestDTO, Long customerId) {
        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. " + reviewId));

        if (!review.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("리뷰를 수정할 수 있는 권한이 없습니다.");
        }

        review.setRating(requestDTO.getRating().intValue()); // 🔧 int 변환
        review.setContent(requestDTO.getContent());

        imageRepository.deleteByReviewId(reviewId);
        List<String> imageUrls = saveImages(review, requestDTO.getImageUrls());

        SellerReview updatedReview = reviewRepository.save(review);

        return ReviewResponseDTO.builder()
                .reviewId(updatedReview.getId())
                .orderId(updatedReview.getOrder().getId())
                .customerId(updatedReview.getCustomer().getId())
                .sellerId(updatedReview.getSeller().getId())
                .productName(null)
                .rating(updatedReview.getRating())
                .content(updatedReview.getContent())
                .imageUrls(imageUrls)
                .createdAt(updatedReview.getCreatedAt())
                .isHidden(updatedReview.isHidden()) // 🔧 수정
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long customerId) {
        SellerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. : " + reviewId));

        if (!review.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("리뷰를 삭제하실 수 있는 권한이 없습니다.");
        }

        imageRepository.deleteByReviewId(reviewId);
<<<<<<< HEAD
=======
        log.info("deleteReview - 리뷰 이미지 DB에서 삭제 완료: reviewId={}", reviewId);

>>>>>>> dev
        reviewRepository.delete(review);
    }

    private List<String> saveImages(SellerReview review, List<String> imageUrls) {
        List<String> savedImageUrls = new ArrayList<>();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<SellerReviewImage> reviewImages = imageUrls.stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> SellerReviewImage.builder()
                            .review(review)
                            .imageUrl(url)
                            .thumbnail(imageUrls.indexOf(url) == 0)
                            .build())
                    .collect(Collectors.toList());
            imageRepository.saveAll(reviewImages);
            savedImageUrls = reviewImages.stream()
                    .map(SellerReviewImage::getImageUrl)
                    .collect(Collectors.toList());
        }
        return savedImageUrls;
    }

    @Override
    public boolean checkReviewExistence(Long orderId, Long customerId) {
<<<<<<< HEAD
        return reviewRepository.findByOrderIdAndCustomerId(orderId, customerId).isPresent();
    }
}
=======
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 고객입니다.: " + customerId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        if (orderItems.isEmpty()) {
            log.warn("checkReviewExistence - 주문 ID {}에 해당하는 OrderItem이 없습니다.", orderId);
            return false;
        }

        // 주문의 첫 번째 아이템의 판매자 ID를 사용하여 해당 주문/고객/판매자 조합의 리뷰 존재 여부를 확인합니다.
        // 이는 createReview의 findByOrder_IdAndCustomer_IdAndSeller_Id와 일관성을 유지하기 위함입니다.
        // 한 주문에 여러 판매자의 상품이 있을 경우, 이 로직은 첫 번째 상품의 판매자에 대한 리뷰만 확인합니다.
        // 모든 판매자에 대한 리뷰 존재 여부를 확인하려면, orderItems를 순회하며 각 판매자에 대해 체크해야 합니다.
        // 현재 프론트엔드 로직은 단일 판매자에 대한 리뷰를 가정하고 있습니다.
        Long sellerIdOfFirstItemInOrder = orderItems.get(0).getProduct().getSeller().getId();

        return reviewRepository.findByOrderIdAndCustomerIdAndSellerId(orderId, customerId, sellerIdOfFirstItemInOrder).isPresent();
    }

    private String getProductNameForReview(Order order, Seller seller) {
        return orderItemRepository.findByOrderId(order.getId()).stream()
                .filter(orderItem -> orderItem.getProduct() != null && orderItem.getProduct().getSeller() != null &&
                        orderItem.getProduct().getSeller().getId().equals(seller.getId()))
                .map(orderItem -> orderItem.getProduct().getName())
                .findFirst()
                .orElse(null);
    }
}
>>>>>>> dev
