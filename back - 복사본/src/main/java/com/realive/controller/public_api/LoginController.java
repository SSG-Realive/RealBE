package com.realive.controller.public_api;


import com.realive.domain.customer.Customer;
import com.realive.dto.customer.login.CustomerLoginRequestDTO;
import com.realive.dto.customer.login.CustomerLoginResponseDTO;
import com.realive.dto.customer.member.MemberJoinDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.security.JwtUtil;
import com.realive.security.customer.CustomerPrincipal;

import jakarta.servlet.http.HttpServletResponse;
import com.realive.service.customer.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/auth")
@Slf4j
// ✅ @RequiredArgsConstructor를 사용하려면 필드를 final로 선언해야 합니다.
@RequiredArgsConstructor
public class LoginController {

    // --- 의존성 주입 부분 ---
    // ✅ @Qualifier는 주입받는 필드 또는 생성자 파라미터에 사용합니다.
    // 여러 UserDetailsService 중 "customUserDetailsService"라는 이름을 가진 Bean을 특정해서 주입받습니다.
    @Qualifier("customUserDetailsService")
    private final UserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MemberService memberService;


@PostMapping("/login")
public ResponseEntity<CustomerLoginResponseDTO> login(
        @RequestBody @Valid CustomerLoginRequestDTO request,
        HttpServletResponse response) {

    // 1. 사용자 조회
    CustomerPrincipal principal =
            (CustomerPrincipal) customUserDetailsService.loadUserByUsername(request.email());

    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(request.password(), principal.getPassword())) {
        throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
    }

    // 3. 토큰 생성
    String accessToken  = jwtUtil.generateAccessToken(principal);
    String refreshToken = jwtUtil.generateRefreshToken(principal);

    // 3-1. 리프레시 토큰 쿠키
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(60 * 60 * 24 * 7)   // 7 일
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    // 3-2. 액세스 토큰 → Authorization 헤더
    response.setHeader(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + accessToken);

    // 4. 응답 DTO (리프레시 토큰은 제외)
    CustomerLoginResponseDTO dto = CustomerLoginResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(null)   // 프런트로 보내지 않음
            .email(principal.getUsername())
            .name(principal.getName())
            .build();

    return ResponseEntity.ok(dto);
}
    // 일반 회원가입
    @PostMapping("/join")
    public ResponseEntity<?> registerMember(@RequestBody @Valid MemberJoinDTO dto) {
        String token = memberService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "회원가입 성공",
                        "token", token
                ));
    }


}