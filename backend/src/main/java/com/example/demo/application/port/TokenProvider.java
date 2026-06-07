package com.example.demo.application.port;

import com.example.demo.domain.customer.Customer;

/**
 * Token 提供者入站端口 — 签发和验证 Token。
 */
public interface TokenProvider {

    /**
     * 为客户签发 JWT token。
     */
    String generateToken(Customer customer);

    /**
     * 验证 token 是否有效。
     */
    boolean validateToken(String token);

    /**
     * 从 token 中解析用户名。
     */
    String getUsernameFromToken(String token);
}
