package com.tlcn.books.config;

import com.tlcn.books.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String accountId;

        // 1. Kiểm tra header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Lấy token
        jwt = authHeader.substring(7);

        try {
            // 3. Giải mã token
            accountId = jwtService.extractUsername(jwt);

            // 4. Kiểm tra user chưa xác thực
            if (accountId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Kiểm tra token hợp lệ
                if (jwtService.isTokenValid(jwt)) {
                    List<SimpleGrantedAuthority> authorities = jwtService.extractAuthorities(jwt);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            accountId,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Set Authentication thành công
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // 🛑 QUAN TRỌNG: SỬA ĐOẠN NÀY
            // Nếu token lỗi (hết hạn, sai format...), TA KHÔNG TRẢ VỀ 401.
            // Ta chỉ xóa context (để đảm bảo an toàn) và coi như user chưa đăng nhập.
            SecurityContextHolder.clearContext();

            // Log ra để debug nếu cần (có thể xóa dòng này khi chạy thật)
            System.out.println("Token error (tiếp tục như khách vãng lai): " + e.getMessage());
        }

        // 7. LUÔN CHO PHÉP REQUEST ĐI TIẾP
        // Dù token đúng hay sai, request vẫn đi tiếp đến SecurityConfig.
        // - Nếu sai token + vào trang public (/api/books) -> SecurityConfig cho qua (OK).
        // - Nếu sai token + vào trang kín (/api/admin) -> SecurityConfig sẽ chặn (403).
        filterChain.doFilter(request, response);
    }
}