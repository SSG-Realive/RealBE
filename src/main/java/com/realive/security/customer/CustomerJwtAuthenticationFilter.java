package com.realive.security.customer;

import com.realive.domain.admin.Admin;
import com.realive.domain.customer.Customer;
import com.realive.security.AdminPrincipal;
import com.realive.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    // 1. Authorization 헤더가 있는지, "Bearer "로 시작하는지 확인
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        log.info("[JWT 디버깅] 추출된 토큰: {}", token);

        // 2. 토큰이 유효한지 확인
        if (jwtUtil.validateToken(token)) {
            log.info("[JWT 디버깅] 토큰이 유효합니다.");
            Claims claims = jwtUtil.getClaims(token);
            String subject = claims.getSubject();

            log.info("[JWT 디버깅] 토큰 Subject: [{}], 기대하는 Subject: [{}]", subject, JwtUtil.SUBJECT_CUSTOMER);

            // 3. 토큰의 subject가 'Customer'용이 맞는지 확인
            if (JwtUtil.SUBJECT_CUSTOMER.equals(subject)) {
                log.info("[JWT 디버깅] Subject가 일치합니다. Customer 토큰으로 처리합니다.");

                String email = claims.get("email", String.class);
                Long customerId = claims.get("id", Long.class);
                String role = claims.get("auth", String.class);

                log.info("[JWT 디버깅] 추출된 Claims - Email: [{}], ID: [{}], Role: [{}]", email, customerId, role);
                
                // 4. 필요한 모든 클레임이 존재하는지 확인
                if (email != null && customerId != null && role != null) {
                    log.info("[JWT 디버깅] 모든 클레임이 존재합니다. 인증 객체를 생성하고 SecurityContext에 저장합니다.");
                    Customer customerForPrincipal = Customer.builder().id(customerId).email(email).build();
                    CustomerPrincipal customerPrincipal = new CustomerPrincipal(customerForPrincipal);
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                    Authentication authentication = new UsernamePasswordAuthenticationToken(customerPrincipal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("[JWT 디버깅] 토큰에 필수 클레임(email, id, auth)이 누락되었습니다.");
                }
            } else {
                log.warn("[JWT 디버깅] Subject가 일치하지 않습니다. 이 필터에서는 처리하지 않습니다.");
            }
        } else {
            log.warn("[JWT 디버깅] 유효하지 않은 토큰입니다.");
        }
    } else {
        log.info("[JWT 디버깅] Authorization 헤더가 없거나 Bearer 타입이 아닙니다.");
    }

    filterChain.doFilter(request, response);
}

     @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        
        // 요청 경로가 "/api/customer/"로 시작하는 경우에만 이 필터가 동작하도록 합니다.
        // 그 외의 경우(예: /api/public, /api/auth)에는 이 필터를 건너뜁니다.
        log.info("[CustomerJwtFilter] shouldNotFilter 검사. URI: {}", uri);
        boolean shouldNotFilter = !uri.startsWith("/api/customer/");
        log.info("필터 실행 여부 (false여야 실행됨): {}", !shouldNotFilter);
        
        return shouldNotFilter;
    }
    
    
}