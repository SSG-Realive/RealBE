package com.realive.admin;

import com.realive.domain.admin.Admin;
import com.realive.repository.admin.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/realive_test",
    "spring.datasource.username=postgres",
    "spring.datasource.password=postgres",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@Transactional
public class AdminSignupTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    public void insertAdmin() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Admin admin = new Admin();
        admin.setEmail("admin@admin.com");
        admin.setName("Gudo");
        admin.setPassword(encoder.encode("admin")); // 패스워드 암호화

        adminRepository.save(admin);

        System.out.println("관리자 데이터 저장 완료");
    }
}