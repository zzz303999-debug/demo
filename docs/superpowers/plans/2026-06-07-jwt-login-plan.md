# JWT 登录功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为网上书店系统新增 `POST /api/auth/login` 接口，使用 JWT 签发 token，遵循 DDD 四层架构。

**Architecture:** 新增独立 Auth 模块（AuthController → AuthUseCase/AuthApplicationService），JWT 签发通过基础设施层 JwtTokenProvider 实现，解耦于现有 Customer 模块。TokenProvider 接口在 application/port 定义，JwtTokenProvider 在 infrastructure 实现，AuthApplicationService 依赖接口而非实现。

**Tech Stack:** Spring Boot 4.0.6, Java 17, jjwt 0.12.6, BCrypt, Lombok, Jakarta Validation

---

### Task 1: 添加 JWT 依赖与配置

**Files:**
- Modify: `build.gradle`
- Modify: `src/main/resources/application.yml`
- Create: `src/main/java/com/example/demo/config/JwtConfig.java`
- Modify: `src/main/java/com/example/demo/DemoApplication.java`

- [ ] **Step 1: 在 build.gradle 中添加 jjwt 依赖**

在 `dependencies` 块末尾（`// Test` 注释之前）添加：

```groovy
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

- [ ] **Step 2: 生成 JWT 密钥并添加到 application.yml**

Run: `openssl rand -base64 32`

将输出作为 secret 值，在 `application.yml` 末尾添加：

```yaml
# JWT 配置
jwt:
  secret: <上一步生成的base64字符串>
  expiration-ms: 86400000
```

- [ ] **Step 3: 创建 JwtConfig.java**

```java
package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(String secret, long expirationMs) {
}
```

- [ ] **Step 4: 修改 DemoApplication.java 启用配置绑定**

```java
package com.example.demo;

import com.example.demo.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```

- [ ] **Step 5: 提交**

```bash
git add build.gradle src/main/resources/application.yml src/main/java/com/example/demo/config/JwtConfig.java src/main/java/com/example/demo/DemoApplication.java
git commit -m "feat: add JWT dependency and configuration"
```

---

### Task 2: 创建 TokenProvider 接口与 JWT 实现

**Files:**
- Create: `src/main/java/com/example/demo/application/port/TokenProvider.java`
- Create: `src/main/java/com/example/demo/infrastructure/auth/JwtTokenProvider.java`

- [ ] **Step 1: 创建 TokenProvider 接口（application/port 层）**

```java
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
```

- [ ] **Step 2: 创建 JwtTokenProvider（infrastructure 层）**

```java
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
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/demo/application/port/TokenProvider.java src/main/java/com/example/demo/infrastructure/auth/JwtTokenProvider.java
git commit -m "feat: add TokenProvider interface and JWT implementation"
```

---

### Task 3: 创建认证异常并更新全局异常处理

**Files:**
- Create: `src/main/java/com/example/demo/application/auth/exception/AuthenticationException.java`
- Modify: `src/main/java/com/example/demo/controller/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 AuthenticationException**

```java
package com.example.demo.application.auth.exception;

/**
 * 认证失败异常 — 用户名或密码错误。
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
```

- [ ] **Step 2: 在 GlobalExceptionHandler 中添加 401 处理**

在 `GlobalExceptionHandler.java` 中的 `handleValidation` 方法之后、`buildError` 方法之前，插入：

```java
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
```

同时添加 import：

```java
import com.example.demo.application.auth.exception.AuthenticationException;
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/demo/application/auth/exception/AuthenticationException.java src/main/java/com/example/demo/controller/GlobalExceptionHandler.java
git commit -m "feat: add AuthenticationException and 401 error handling"
```

---

### Task 4: 创建 LoginRequest 和 LoginResponse DTO

**Files:**
- Create: `src/main/java/com/example/demo/application/auth/dto/LoginRequest.java`
- Create: `src/main/java/com/example/demo/application/auth/dto/LoginResponse.java`

- [ ] **Step 1: 创建 LoginRequest.java**

```java
package com.example.demo.application.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度 3-50 字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度 6-100 字符")
    private String password;
}
```

- [ ] **Step 2: 创建 LoginResponse.java**

```java
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
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/demo/application/auth/dto/LoginRequest.java src/main/java/com/example/demo/application/auth/dto/LoginResponse.java
git commit -m "feat: add LoginRequest and LoginResponse DTOs"
```

---

### Task 5: 创建 AuthUseCase 端口与 AuthApplicationService

**Files:**
- Create: `src/main/java/com/example/demo/application/port/AuthUseCase.java`
- Create: `src/main/java/com/example/demo/application/auth/AuthApplicationService.java`

- [ ] **Step 1: 创建 AuthUseCase 端口接口**

```java
package com.example.demo.application.port;

import com.example.demo.application.auth.dto.LoginRequest;
import com.example.demo.application.auth.dto.LoginResponse;

/**
 * 认证入站端口 — 定义认证相关用例。
 */
public interface AuthUseCase {

    /**
     * 用户登录，验证用户名密码后返回 JWT token 及用户信息。
     */
    LoginResponse login(LoginRequest request);
}
```

- [ ] **Step 2: 创建 AuthApplicationService**

```java
package com.example.demo.application.auth;

import com.example.demo.application.auth.dto.LoginRequest;
import com.example.demo.application.auth.dto.LoginResponse;
import com.example.demo.application.auth.exception.AuthenticationException;
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
}
```

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/demo/application/port/AuthUseCase.java src/main/java/com/example/demo/application/auth/AuthApplicationService.java
git commit -m "feat: add AuthUseCase port and AuthApplicationService"
```

---

### Task 6: 创建 AuthController

**Files:**
- Create: `src/main/java/com/example/demo/controller/AuthController.java`

- [ ] **Step 1: 创建 AuthController.java**

```java
package com.example.demo.controller;

import com.example.demo.application.auth.dto.LoginRequest;
import com.example.demo.application.auth.dto.LoginResponse;
import com.example.demo.application.port.AuthUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Web 适配器 — 将 HTTP 请求适配到 AuthUseCase 入站端口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/example/demo/controller/AuthController.java
git commit -m "feat: add AuthController with POST /api/auth/login"
```

---

### Task 7: 编写登录接口测试

**Files:**
- Create: `src/test/java/com/example/demo/AuthApiTest.java`

- [ ] **Step 1: 创建 AuthApiTest.java**

遵循现有测试风格：`RestTemplate` + 真实 HTTP 请求 + H2 内存数据库 + AssertJ。

```java
package com.example.demo;

import com.example.demo.application.auth.dto.LoginRequest;
import com.example.demo.application.auth.dto.LoginResponse;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 认证 API 集成测试 — 真实 HTTP 请求测试 AuthController 登录端点。
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

    {
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
            }
        });
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ==================== 前提：注册测试用户 ====================

    @Test
    @Order(1)
    void registerTestUser_ShouldReturn201() {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("logintest");
        body.setPassword("123456");
        body.setEmail("logintest@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/customers"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    // ==================== POST /api/auth/login ====================

    @Test
    @Order(2)
    void login_ShouldReturn200WithToken() {
        LoginRequest body = new LoginRequest("logintest", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<LoginResponse> resp = restTemplate.exchange(
                url("/api/auth/login"), HttpMethod.POST, req, LoginResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).startsWith("eyJ");
        assertThat(resp.getBody().getUsername()).isEqualTo("logintest");
        assertThat(resp.getBody().getEmail()).isEqualTo("logintest@example.com");
    }

    @Test
    @Order(3)
    void login_WrongPassword_ShouldReturn401() {
        LoginRequest body = new LoginRequest("logintest", "wrongpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/login"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).contains("用户名或密码错误");
    }

    @Test
    @Order(4)
    void login_NonexistentUser_ShouldReturn401() {
        LoginRequest body = new LoginRequest("nobody", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/login"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).contains("用户名或密码错误");
    }

    @Test
    @Order(5)
    void login_EmptyUsername_ShouldReturn400() {
        LoginRequest body = new LoginRequest("", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/login"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("用户名不能为空");
    }

    @Test
    @Order(6)
    void login_ShortPassword_ShouldReturn400() {
        LoginRequest body = new LoginRequest("logintest", "12");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/auth/login"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("密码长度");
    }
}
```

- [ ] **Step 2: 运行测试并确认全部通过**

Run: `./gradlew test --tests AuthApiTest`

Expected: 所有 6 个测试 PASS。

- [ ] **Step 3: 运行全部已有测试确保无回归**

Run: `./gradlew test`

Expected: 全部测试 PASS。

- [ ] **Step 4: 提交**

```bash
git add src/test/java/com/example/demo/AuthApiTest.java
git commit -m "test: add AuthApiTest for login endpoint"
```

---

### Task 8: 创建 HTTP 测试文件与最终验证

**Files:**
- Create: `http/auth.http`

- [ ] **Step 1: 创建 auth.http 手动测试文件**

```http
@baseUrl = http://localhost:8080

### 1. 登录成功 (200 OK)
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456"
}

### 2. 登录失败 - 密码错误 (401 Unauthorized)
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "wrongpassword"
}

### 3. 登录失败 - 用户不存在 (401 Unauthorized)
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "nobody",
  "password": "123456"
}

### 4. 登录失败 - 用户名为空 (400 Bad Request)
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "",
  "password": "123456"
}

### 5. 登录失败 - 密码过短 (400 Bad Request)
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "12"
}
```

- [ ] **Step 2: 提交**

```bash
git add http/auth.http
git commit -m "docs: add auth.http manual test file"
```

---

### 最终文件结构

```
新增文件:
  src/main/java/com/example/demo/config/JwtConfig.java
  src/main/java/com/example/demo/application/port/TokenProvider.java
  src/main/java/com/example/demo/application/port/AuthUseCase.java
  src/main/java/com/example/demo/application/auth/AuthApplicationService.java
  src/main/java/com/example/demo/application/auth/dto/LoginRequest.java
  src/main/java/com/example/demo/application/auth/dto/LoginResponse.java
  src/main/java/com/example/demo/application/auth/exception/AuthenticationException.java
  src/main/java/com/example/demo/infrastructure/auth/JwtTokenProvider.java
  src/main/java/com/example/demo/controller/AuthController.java
  src/test/java/com/example/demo/AuthApiTest.java
  http/auth.http

修改文件:
  build.gradle                             — 添加 jjwt 依赖
  src/main/resources/application.yml       — 添加 jwt 配置
  src/main/java/com/example/demo/DemoApplication.java  — @EnableConfigurationProperties
  src/main/java/com/example/demo/controller/GlobalExceptionHandler.java — 401 处理
```
