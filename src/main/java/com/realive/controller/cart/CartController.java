package com.realive.controller.cart;

import com.realive.dto.cart.CartItemAddRequestDTO;
import com.realive.dto.cart.CartItemResponseDTO;
import com.realive.dto.cart.CartItemUpdateRequestDTO;
import com.realive.dto.cart.CartListResponseDTO;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.service.cart.crud.CartService;
import com.realive.service.cart.view.CartViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
@Log4j2
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartViewService cartViewService;

    //장바구니 추가
    @PostMapping
    public ResponseEntity<CartItemResponseDTO> addCartItem(
            @AuthenticationPrincipal MemberLoginDTO userDetails,
            @Valid @RequestBody CartItemAddRequestDTO requestDTO) {
        log.info("Request to add item to cart: {}", requestDTO);
        CartItemResponseDTO response = cartService.addCartItem(userDetails.getId(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //장바구니 리스트 조회
    @GetMapping
    public ResponseEntity<CartListResponseDTO> getCart(
            @AuthenticationPrincipal MemberLoginDTO userDetails) {
        log.info("Request to view cart list.");
        CartListResponseDTO response = cartViewService.getCart(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    //장바구니 수량 변경
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateCartItem(
            @AuthenticationPrincipal MemberLoginDTO userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequestDTO requestDTO) {
        log.info("Request to update cart item {}: {}", cartItemId, requestDTO);
        CartItemResponseDTO response = cartService.updateCartItemQuantity(
                userDetails.getId(), cartItemId, requestDTO);

        if (response == null) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(response);
    }

    //장바구니 물품 삭제
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @AuthenticationPrincipal MemberLoginDTO userDetails,
            @PathVariable Long cartItemId) {
        log.info("Request to remove cart item: {}", cartItemId);
        cartService.removeCartItem(userDetails.getId(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    //장바구니 물품 모두 삭제
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal MemberLoginDTO userDetails) {
        log.info("Request to clear cart.");
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}