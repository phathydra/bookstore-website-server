package com.tlcn.books.config; // Đặt chung package với SecurityConfig

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
        final String accountId; // Username (ví dụ: email) chính là accountId

        // 1. Kiểm tra header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Không có token, cho qua
            return;
        }

        // 2. Lấy token
        jwt = authHeader.substring(7); // Bỏ "Bearer "

        try {
            // 3. Giải mã token để lấy accountId
            accountId = jwtService.extractUsername(jwt);

            // 4. Kiểm tra xem user đã được xác thực chưa
            if (accountId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Kiểm tra token có hợp lệ không
                if (jwtService.isTokenValid(jwt)) {
                    // Lấy quyền (roles) từ token
                    List<SimpleGrantedAuthority> authorities = jwtService.extractAuthorities(jwt);

                    // Tạo đối tượng xác thực
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            accountId, // Đây chính là Principal
                            null,
                            authorities // Quyền
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 6. ĐƯA USER VÀO SECURITY CONTEXT
                    // Đây là bước mấu chốt để Principal hoạt động
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Nếu token sai, hết hạn...
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }
    }
}