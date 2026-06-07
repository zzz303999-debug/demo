# JWT 登录功能设计文档

## 1. 概述

为网上书店系统新增 JWT 登录接口，遵循现有 DDD 四层架构，参考 AuthController 模式，保持代码解耦和规范。

## 2. 现有上下文

- Spring Boot 4.0.6, Java 17, Gradle
- DDD 四层：Controller → Application (port + service + DTO) → Domain (entity + repository interface) → Infrastructure (JPA impl)
- 已有 `spring-security-crypto`（BCrypt），无 Spring Security
- `Customer` 实体已有 `username` / `password` 字段
- `CustomerRepository` 已有 `findByUsername(String)`
- `CustomerApplicationService` 已注入 `BCryptPasswordEncoder`

## 3. 新增依赖

```groovy
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

## 4. 配置

`application.yml`:
```yaml
jwt:
  secret: <base64-encoded-256bit-secret>
  expiration-ms: 86400000  # 24h
```

## 5. 新增文件

```
src/main/java/com/example/demo/
├── config/
│   └── JwtConfig.java              ← @ConfigurationProperties("jwt")
├── infrastructure/auth/
│   └── JwtTokenProvider.java       ← JWT 签发/解析（基础设施层）
├── application/auth/
│   ├── dto/
│   │   ├── LoginRequest.java       ← 请求 DTO（含校验）
│   │   └── LoginResponse.java      ← 响应 DTO（token + 客户信息）
│   └── AuthApplicationService.java ← 登录业务逻辑
├── application/port/
│   └── AuthUseCase.java            ← 入站端口接口
├── controller/
│   └── AuthController.java         ← POST /api/auth/login
```

## 6. 修改文件

| 文件 | 变更 |
|---|---|
| `build.gradle` | 添加 jjwt 依赖 |
| `application.yml` | 添加 jwt 配置段 |
| `DemoApplication.java` | 添加 `@EnableConfigurationProperties(JwtConfig.class)` |
| `GlobalExceptionHandler.java` | 新增 `@ExceptionHandler` 处理认证失败（401） |

## 7. 接口规格

### POST /api/auth/login

**Request:**
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "id": 1,
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "phone": "13800001111",
  "address": "北京市海淀区",
  "createdAt": "2026-06-07T10:30:00.123"
}
```

**错误:**
| 状态码 | 条件 | message |
|--------|------|---------|
| 400 | 参数校验失败 | field: msg |
| 401 | 用户名或密码错误 | `用户名或密码错误` |

## 8. JWT Payload

```json
{
  "sub": "1",
  "username": "zhangsan",
  "iat": 1718123456,
  "exp": 1718209856
}
```

## 9. 设计原则

- **解耦**: 登录逻辑独立于现有 CustomerUseCase，通过 AuthUseCase 端口 + AuthApplicationService 实现
- **分层**: JWT 工具类放在 infrastructure 层，业务逻辑在 application 层，Controller 只做参数适配
- **复用**: JwtTokenProvider 可在后续拦截器/过滤器中复用
- **一致**: DTO 命名、异常处理、响应格式与现有代码保持一致
