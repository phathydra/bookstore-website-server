package com.bookstore.Shipping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

/**
 * Lớp cấu hình Spring để định nghĩa các Beans cần thiết cho ứng dụng.
 */
@Configuration
public class AppConfig {

    /**
     * Định nghĩa Bean PasswordEncoder sử dụng thuật toán BCrypt.
     * Bean này được tiêm vào các Service (như AccountServiceImpl và ShippingServiceImpl)
     * để mã hóa và so sánh mật khẩu.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder là thuật toán mã hóa mật khẩu được khuyên dùng
        // trong Spring Security.
        return new BCryptPasswordEncoder();
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
