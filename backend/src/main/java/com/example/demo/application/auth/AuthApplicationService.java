package com.example.demo.application.auth;

import com.example.demo.application.auth.dto.LoginRequest;
import com.example.demo.application.auth.dto.LoginResponse;
import com.example.demo.application.auth.exception.AuthenticationException;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import com.example.demo.application.port.AuthUseCase;
import com.example.demo.application.port.TokenProvider;
import com.example.demo.domain.customer.Customer;
import com.example.demo.domain.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务 — 实现 AuthUseCase 入站端口，管理事务边界。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthApplicationService implements AuthUseCase {

    private final CustomerRepository customerRepository;
    private final TokenProvider tokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new AuthenticationException("用户名或密码错误");
        }

        String token = tokenProvider.generateToken(customer);
        return LoginResponse.of(customer, token);
    }

    @Override
    public LoginResponse register(RegisterCustomerRequest request) {
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("用户名已存在：" + request.getUsername());
        }

        Customer customer = Customer.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        Customer saved = customerRepository.save(customer);
        String token = tokenProvider.generateToken(saved);
        return LoginResponse.of(saved, token);
    }
}
