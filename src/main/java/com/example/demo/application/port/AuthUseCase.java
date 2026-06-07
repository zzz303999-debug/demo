package com.example.demo.application.port;

import com.example.demo.application.auth.dto.LoginRequest;
import com.example.demo.application.auth.dto.LoginResponse;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;

/**
 * 认证入站端口 — 定义登录与注册用例。
 */
public interface AuthUseCase {

    /**
     * 用户登录，验证用户名密码后返回 JWT token 及用户信息。
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册，注册成功后自动签发 JWT token。
     */
    LoginResponse register(RegisterCustomerRequest request);
}
