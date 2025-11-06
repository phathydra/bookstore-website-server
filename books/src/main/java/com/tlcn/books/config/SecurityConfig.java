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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Sử dụng bean CORS bên dưới

                .authorizeHttpRequests(authorize -> authorize
                        // Luôn cho phép các request OPTIONS (cho CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Các đường dẫn public
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // === CÁC API PUBLIC (KHÔNG CẦN ĐĂNG NHẬP) ===
                        // 1. Ai cũng được GET sách, review, analytics, summary
                        .requestMatchers(HttpMethod.GET, "/api/book/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/book/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/analytics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/summary").permitAll()

                        // 2. Cho phép TẤT CẢ request POST (để track analytics)
                        .requestMatchers(HttpMethod.POST, "/api/analytics/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/book/filter").permitAll() // Cho phép lọc

                        // 3. (Test) Cho phép admin tạm thời
                        .requestMatchers(HttpMethod.POST, "/api/book/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/book/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/book/**").permitAll()

                        // 4. Bất kỳ request nào khác vẫn BẮT BUỘC đăng nhập
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Filter JWT vẫn được bật để lấy accountId (nếu có)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean CORS giữ nguyên
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001", "http://localhost:3002"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
