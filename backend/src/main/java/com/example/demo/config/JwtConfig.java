package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(String secret, long expirationMs) {
}
