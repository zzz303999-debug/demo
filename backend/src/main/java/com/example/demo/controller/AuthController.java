package com.example.demo.controller;

import com.example.demo.application.auth.dto.LoginRequest;
import com.example.demo.application.auth.dto.LoginResponse;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import com.example.demo.application.port.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Web 适配器 — 将 HTTP 请求适配到 AuthUseCase 入站端口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册和登录接口")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，验证用户名密码后返回 JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
        @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authUseCase.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "用户注册", description = "创建新用户账户，注册成功后自动签发 JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "注册成功，返回 token"),
        @ApiResponse(responseCode = "409", description = "用户名已存在"),
        @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    public LoginResponse register(@Valid @RequestBody RegisterCustomerRequest request) {
        return authUseCase.register(request);
    }
}
