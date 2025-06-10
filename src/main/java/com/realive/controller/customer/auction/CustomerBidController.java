package com.realive.controller.customer.auction;

import com.realive.dto.bid.BidRequestDTO;
import com.realive.dto.bid.BidResponseDTO;
import com.realive.dto.common.ApiResponse;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.service.admin.auction.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/bids")
@RequiredArgsConstructor
@Slf4j
public class CustomerBidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<ApiResponse<BidResponseDTO>> placeBid(
            @Valid @RequestBody BidRequestDTO requestDto
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "고객 로그인이 필요합니다."));
            }
            
            MemberLoginDTO userDetails = (MemberLoginDTO) authentication.getPrincipal();
            Long customerId = userDetails.getId();
            
            log.info("POST /api/customer/bids - 입찰 요청 데이터: auctionId={}, bidPrice={}, CustomerId={}", 
                requestDto.getAuctionId(), requestDto.getBidPrice(), customerId);
            
            if (requestDto.getAuctionId() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "경매 ID는 필수입니다."));
            }
            
            if (requestDto.getBidPrice() == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "입찰 가격은 필수입니다."));
            }
            
            BidResponseDTO placedBid = bidService.placeBid(requestDto, customerId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("입찰이 성공적으로 등록되었습니다.", placedBid));
        } catch (AccessDeniedException e) {
            log.error("입찰 권한 없음 - 요청: {}, CustomerId: {}", requestDto, 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("입찰 중 알 수 없는 오류 발생 - 요청: {}, CustomerId: {}", requestDto, 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "입찰 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/my-bids")
    public ResponseEntity<Page<BidResponseDTO>> getMyBids(
            @PageableDefault(size = 20, sort = "bidTime", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            MemberLoginDTO userDetails = (MemberLoginDTO) authentication.getPrincipal();
            Long customerId = userDetails.getId();
            
            log.info("GET /api/customer/bids/my-bids - 나의 입찰 내역 조회 요청. CustomerId: {}", customerId);
            Page<BidResponseDTO> bids = bidService.getBidsByCustomer(customerId, pageable);
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            log.error("나의 입찰 내역 조회 중 알 수 없는 오류 발생 - CustomerId: {}", 
                SecurityContextHolder.getContext().getAuthentication() != null ? 
                ((MemberLoginDTO)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId() : null, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 