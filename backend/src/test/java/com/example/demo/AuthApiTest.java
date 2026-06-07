package com.example.demo;

import com.example.demo.application.auth.dto.LoginRequest;
import com.example.demo.application.auth.dto.LoginResponse;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 认证 API 集成测试 — 测试 AuthController 注册与登录端点。
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false"
    }
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthApiTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }

    // ==================== POST /api/auth/register ====================

    @Test
    @Order(1)
    void register_ShouldReturn201WithToken() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("logintest");
        body.setPassword("123456");
        body.setEmail("logintest@example.com");

        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, jsonHeaders());

        ResponseEntity<LoginResponse> resp = restTemplate.exchange(
                url("/api/auth/register"), HttpMethod.POST, req, LoginResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).startsWith("eyJ");
        assertThat(resp.getBody().getUsername()).isEqualTo("logintest");
        assertThat(resp.getBody().getEmail()).isEqualTo("logintest@example.com");
    }

    @Test
    @Order(2)
    void register_DuplicateUsername_ShouldReturn409() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("logintest");
        body.setPassword("123456");

        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, jsonHeaders());

        assertThatThrownBy(() ->
                restTemplate.exchange(url("/api/auth/register"), HttpMethod.POST, req, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting(e -> ((HttpClientErrorException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(3)
    void register_ShortUsername_ShouldReturn400() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("ab");
        body.setPassword("123456");

        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, jsonHeaders());

        assertThatThrownBy(() ->
                restTemplate.exchange(url("/api/auth/register"), HttpMethod.POST, req, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting(e -> ((HttpClientErrorException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(4)
    void register_BlankUsername_ShouldReturn400() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("");
        body.setPassword("123456");

        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, jsonHeaders());

        assertThatThrownBy(() ->
                restTemplate.exchange(url("/api/auth/register"), HttpMethod.POST, req, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting(e -> ((HttpClientErrorException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(5)
    void register_ShortPassword_ShouldReturn400() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("newuser");
        body.setPassword("12");

        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, jsonHeaders());

        assertThatThrownBy(() ->
                restTemplate.exchange(url("/api/auth/register"), HttpMethod.POST, req, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting(e -> ((HttpClientErrorException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== POST /api/auth/login ====================

    @Test
    @Order(6)
    void login_ShouldReturn200WithToken() {
        LoginRequest body = new LoginRequest("logintest", "123456");

        HttpEntity<LoginRequest> req = new HttpEntity<>(body, jsonHeaders());

        ResponseEntity<LoginResponse> resp = restTemplate.exchange(
                url("/api/auth/login"), HttpMethod.POST, req, LoginResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).startsWith("eyJ");
        assertThat(resp.getBody().getUsername()).isEqualTo("logintest");
        assertThat(resp.getBody().getEmail()).isEqualTo("logintest@example.com");
    }

    @Test
    @Order(7)
    void login_WrongPassword_ShouldReturn401() {
        LoginRequest body = new LoginRequest("logintest", "wrongpassword");
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, jsonHeaders());

        assertThatThrownBy(() ->
                restTemplate.exchange(url("/api/auth/login"), HttpMethod.POST, req, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting(e -> ((HttpClientErrorException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(8)
    void login_NonexistentUser_ShouldReturn401() {
        LoginRequest body = new LoginRequest("nobody", "123456");
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, jsonHeaders());

        assertThatThrownBy(() ->
                restTemplate.exchange(url("/api/auth/login"), HttpMethod.POST, req, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting(e -> ((HttpClientErrorException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(9)
    void login_EmptyUsername_ShouldReturn400() {
        LoginRequest body = new LoginRequest("", "123456");
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, jsonHeaders());

        assertThatThrownBy(() ->
                restTemplate.exchange(url("/api/auth/login"), HttpMethod.POST, req, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting(e -> ((HttpClientErrorException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(10)
    void login_ShortPassword_ShouldReturn400() {
        LoginRequest body = new LoginRequest("logintest", "12");
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, jsonHeaders());

        assertThatThrownBy(() ->
                restTemplate.exchange(url("/api/auth/login"), HttpMethod.POST, req, String.class))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting(e -> ((HttpClientErrorException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
