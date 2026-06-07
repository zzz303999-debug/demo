package com.example.demo.application.auth.exception;

/**
 * 认证失败异常 — 用户名或密码错误。
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
