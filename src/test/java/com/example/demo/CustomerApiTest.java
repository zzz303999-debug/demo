package com.example.demo;

import com.example.demo.application.auth.dto.LoginResponse;
import com.example.demo.application.customer.dto.CustomerResponse;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import com.example.demo.application.customer.dto.UpdateCustomerRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 客户 API 集成测试 — 真实 HTTP 请求测试 CustomerController 所有端点。
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
class CustomerApiTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    {
        // 不自动抛异常，让测试自己检查状态码
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
            }
        });
    }

    private static Long customerId;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ==================== POST /api/auth/register（注册已迁移至 AuthController） ====================

    @Test
    @Order(1)
    void register_ShouldReturn201() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("testuser");
        body.setPassword("123456");
        body.setEmail("test@example.com");
        body.setPhone("13800000000");
        body.setAddress("北京市");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<LoginResponse> resp = restTemplate.exchange(
                url("/api/auth/register"), HttpMethod.POST, req, LoginResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).startsWith("eyJ");
        assertThat(resp.getBody().getUsername()).isEqualTo("testuser");
        assertThat(resp.getBody().getEmail()).isEqualTo("test@example.com");
        customerId = resp.getBody().getId();
    }

    @Test
    @Order(2)
    void register_DuplicateUsername_ShouldReturn409() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("testuser");
        body.setPassword("123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/register"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).contains("用户名已存在");
    }

    @Test
    @Order(3)
    void register_ShortUsername_ShouldReturn400() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("ab");
        body.setPassword("123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/register"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("用户名长度");
    }

    @Test
    @Order(4)
    void register_BlankUsername_ShouldReturn400() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("");
        body.setPassword("123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/register"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("用户名不能为空");
    }

    @Test
    @Order(5)
    void register_ShortPassword_ShouldReturn400() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("newuser");
        body.setPassword("12345");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/register"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("密码长度");
    }

    @Test
    @Order(6)
    void register_InvalidEmail_ShouldReturn400() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("newuser");
        body.setPassword("123456");
        body.setEmail("not-an-email");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/register"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("邮箱格式");
    }

    // ==================== GET /api/customers/{id} ====================

    @Test
    @Order(7)
    void getById_ShouldReturn200() {
        ResponseEntity<CustomerResponse> resp = restTemplate.exchange(
                url("/api/customers/" + customerId), HttpMethod.GET, null, CustomerResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getId()).isEqualTo(customerId);
        assertThat(resp.getBody().getUsername()).isEqualTo("testuser");
        assertThat(resp.getBody().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @Order(8)
    void getById_NotFound_ShouldReturn404() {
        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/customers/99999"), HttpMethod.GET, null, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("客户不存在");
    }

    // ==================== PUT /api/customers/{id} ====================

    @Test
    @Order(9)
    void updateProfile_ShouldReturn200() {
        UpdateCustomerRequest body = new UpdateCustomerRequest();
        body.setEmail("updated@example.com");
        body.setPhone("13900001111");
        body.setAddress("上海市");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<CustomerResponse> resp = restTemplate.exchange(
                url("/api/customers/" + customerId), HttpMethod.PUT, req, CustomerResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getId()).isEqualTo(customerId);
        assertThat(resp.getBody().getEmail()).isEqualTo("updated@example.com");
        assertThat(resp.getBody().getPhone()).isEqualTo("13900001111");
        assertThat(resp.getBody().getAddress()).isEqualTo("上海市");
        assertThat(resp.getBody().getUsername()).isEqualTo("testuser");
    }

    @Test
    @Order(10)
    void updateProfile_NotFound_ShouldReturn404() {
        UpdateCustomerRequest body = new UpdateCustomerRequest();
        body.setEmail("test@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/customers/99999"), HttpMethod.PUT, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("客户不存在");
    }
}
