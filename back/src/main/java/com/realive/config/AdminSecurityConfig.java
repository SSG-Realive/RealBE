//package com.realive.config;
//
//import com.realive.security.AdminJwtAuthenticationFilter;
//import com.realive.security.JwtUtil;
//import com.realive.service.admin.AdminService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//@Configuration
//@RequiredArgsConstructor
//@Slf4j
//@Order(0)
//public class AdminSecurityConfig {
//
//    private final JwtUtil jwtUtil;
//    private final AdminService adminService;
//
//    @Bean
//    public AdminJwtAuthenticationFilter adminJwtAuthenticationFilter() {
//        return new AdminJwtAuthenticationFilter(jwtUtil, adminService);
//    }
//
//    @Bean
//    public SecurityFilterChain adminFilterChain(HttpSecurity http,
//                                                AdminJwtAuthenticationFilter adminJwtAuthenticationFilter) throws Exception {
//        log.info("✅ AdminSecurityConfig 적용");
//
//        http
//                .securityMatcher("/api/admin/**")
//                .csrf(csrf -> csrf.disable())
//                .formLogin(form -> form.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/admin/login").permitAll()
//                        .requestMatchers("/api/admin/**").authenticated()
//                );
//
//        return http.build();
//    }
//}