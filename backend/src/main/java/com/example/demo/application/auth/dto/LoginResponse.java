package com.example.demo.application.auth.dto;

import com.example.demo.domain.customer.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录响应 — token + 客户信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;

    public static LoginResponse of(Customer customer, String token) {
        return LoginResponse.builder()
                .token(token)
                .id(customer.getId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
