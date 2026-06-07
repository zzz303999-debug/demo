package com.example.demo.infrastructure.auth;

import com.example.demo.application.port.TokenProvider;
import com.example.demo.config.JwtConfig;
import com.example.demo.domain.customer.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT Token 提供者实现 — 使用 HMAC-SHA256 签发和验证 JWT。
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public String generateToken(Customer customer) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.expirationMs());

        return Jwts.builder()
                .subject(customer.getId().toString())
                .claim("username", customer.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtConfig.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
