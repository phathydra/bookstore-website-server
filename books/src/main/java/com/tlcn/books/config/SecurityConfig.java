package com.tlcn.books.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Tạm tắt import này
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Vẫn giữ biến này để không lỗi khởi động, nhưng sẽ không dùng trong chuỗi filter
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Kích hoạt cấu hình CORS mở rộng

                .authorizeHttpRequests(authorize -> authorize
                        // QUAN TRỌNG: Cho phép TẤT CẢ các request đi qua mà không cần xác thực
                        .anyRequest().permitAll()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // QUAN TRỌNG: Comment dòng này lại để tắt hoàn toàn việc kiểm tra Token
        // http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Cho phép mọi Domain/Port (Thay vì liệt kê localhost cụ thể)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 2. Cho phép mọi Method (GET, POST, PUT, DELETE, OPTIONS, PATCH...)
        configuration.setAllowedMethods(List.of("*"));

        // 3. Cho phép mọi Header
        configuration.setAllowedHeaders(List.of("*"));

        // 4. Cho phép gửi credentials (cookies, auth headers...)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cho tất cả đường dẫn
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}