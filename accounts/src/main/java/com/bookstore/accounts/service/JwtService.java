package com.bookstore.accounts.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret.key}") // Lấy key từ application.yml
    private String SECRET_KEY;

    // === HÀM TẠO TOKEN (QUAN TRỌNG) ===
    public String generateToken(String accountId, String role) {
        Map<String, Object> claims = new HashMap<>();

        // Gói 'role' vào trong một cấu trúc list để
        // service 'books' có thể đọc được (vì nó đang mong chờ 1 list)
        List<Map<String, String>> rolesList = List.of(Map.of("authority", role));
        claims.put("roles", rolesList);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(accountId) // accountId được lưu làm "subject"
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Set thời gian hết hạn (ví dụ: 10 giờ)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // === CÁC HÀM HỖ TRỢ ===

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Các hàm đọc token (nếu bạn cần dùng ở service này)
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
}