package com.tlcn.books.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép các yêu cầu Pre-flight (OPTIONS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth & Swagger
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // === BOOK & CATALOG ===
                        .requestMatchers(HttpMethod.GET, "/api/book/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/book/filter").permitAll()

                        // === ANALYTICS & DASHBOARD (FIX LỖI 403) ===
                        .requestMatchers(HttpMethod.GET, "/api/analytics/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/analytics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/summary").permitAll()

                        // === IMPORTS (QUẢN LÝ NHẬP HÀNG - FIX DÒNG NÀY) ===
                        .requestMatchers("/api/imports/**").permitAll()

                        // === REVIEWS ===
                        .requestMatchers(HttpMethod.GET, "/api/reviews/book/**").permitAll()

                        // === BANNER ===
                        .requestMatchers(HttpMethod.GET, "/api/banners/active").permitAll()
                        .requestMatchers("/api/banners/**").permitAll()

                        // === BOOK MANAGEMENT (Dành cho Admin/Test) ===
                        .requestMatchers(HttpMethod.POST, "/api/book/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/book/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/book/**").permitAll()

                        // === DISCOUNT ===
                        .requestMatchers("/api/discounts/**").permitAll()

                        // Nếu Dashboard có gọi thêm Orders hoặc Users thì bổ sung tại đây:
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()

                        // Tất cả các yêu cầu khác phải xác thực (Nếu chưa permitAll ở trên)
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cấu hình các domain Frontend được phép truy cập
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:3002",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:3001",
                "http://127.0.0.1:3002"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}