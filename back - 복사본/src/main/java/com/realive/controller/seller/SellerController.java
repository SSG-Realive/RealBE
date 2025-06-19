package com.realive.controller.seller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.realive.domain.seller.Seller;
import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.dto.seller.SellerResponseDTO;
import com.realive.dto.seller.SellerSignupDTO;
import com.realive.dto.seller.SellerUpdateDTO;
import com.realive.event.FileUploadEvnetPublisher;
import com.realive.repository.seller.SellerRepository;
import com.realive.security.JwtUtil;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.seller.SellerService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Slf4j
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final JwtUtil jwtUtil;
    private final FileUploadEvnetPublisher fileUploadEvnetPublisher;
    private final SellerRepository sellerRepository;

    // 🔐 로그인 (토큰 발급)
    @PostMapping("/login")
    public ResponseEntity<SellerLoginResponseDTO> login(@RequestBody @Valid SellerLoginRequestDTO request, HttpServletResponse response) {
    
    // 1. 서비스에서 두 토큰을 받아옴
    SellerLoginResponseDTO tokens = sellerService.login(request);

    // 2. 리프레시 토큰 → **HTTP-only 쿠키**로만 내려보냄
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
            .httpOnly(true)         // JS 접근 차단
            .secure(true)           // HTTPS 전용
            .sameSite("Lax")        // POST 리다이렉트 허용
            .path("/")              // 전체 경로
            .maxAge(60 * 60 * 24 * 7) // 7일
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    // 3. 액세스 토큰은 **Authorization 헤더**로 넣어 줌
    response.setHeader(HttpHeaders.AUTHORIZATION,
            JwtUtil.BEARER_PREFIX + tokens.getAccessToken());

    // 4. 프런트에 리프레시 토큰은 굳이 보낼 필요 없으므로 null 처리
    tokens.setRefreshToken(null);

    return ResponseEntity.ok(tokens); // 액세스 토큰만 본문에 포함
    }

    // 로그아웃 (토큰 덮어쓰기)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        response.setHeader("Set-Cookie", deleteCookie.toString());

        return ResponseEntity.noContent().build();
    }

    // 📝 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @RequestPart @Valid SellerSignupDTO dto,
            @RequestPart MultipartFile businessLicense,
            @RequestPart MultipartFile bankAccountCopy) {

        Seller savedSeller = sellerService.registerSeller(dto);

        fileUploadEvnetPublisher.publish(savedSeller, businessLicense, bankAccountCopy);
        return ResponseEntity.ok().build();
    }

    //판매자 정보 보기
    @GetMapping("/me")
    public ResponseEntity<SellerResponseDTO> getMyInfo(@AuthenticationPrincipal SellerPrincipal principal) {
        Long sellerId = principal.getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));

        log.info("Seller email from @AuthenticationPrincipal: {}", seller.getEmail());

        SellerResponseDTO dto = sellerService.getMyInfo(seller);
        return ResponseEntity.ok(dto);
    }

    // 🔄 판매자 정보 수정 - @AuthenticationPrincipal 사용
    @PutMapping("/me")
    public ResponseEntity<Void> updateSeller(
            @AuthenticationPrincipal SellerPrincipal principal, // 파라미터로 주입
            @RequestBody @Valid SellerUpdateDTO dto) {

        Long sellerId = principal.getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));
    

        sellerService.updateSeller(seller, dto);
        return ResponseEntity.ok().build();
    }
}
