# 网上书店管理系统

基于 Spring Boot + Vue 3 的网上书店管理系统，采用 DDD 四层架构和 Feature-Sliced 前端模块化设计。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端框架 | Spring Boot 4.0.6, Java 17 |
| 构建工具 | Gradle |
| 数据库 | PostgreSQL + Flyway 迁移 |
| ORM | Spring Data JPA (Hibernate) |
| 认证 | JWT (jjwt) + BCrypt |
| API 文档 | SpringDoc OpenAPI |
| 前端框架 | Vue 3 + TypeScript + Vite |
| UI 组件库 | Ant Design Vue 4 |
| 状态管理 | Pinia |
| HTTP 客户端 | Axios |

## 项目结构

```
demo/
├── backend/                          # 后端（Spring Boot DDD）
│   ├── src/main/java/com/example/demo/
│   │   ├── config/                   #   配置
│   │   ├── controller/               #   接口层（REST Controller）
│   │   ├── application/              #   应用层（Service + DTO + Port）
│   │   │   ├── auth/                 #     认证模块
│   │   │   ├── customer/             #     客户模块
│   │   │   ├── order/               #     订单模块
│   │   │   ├── product/             #     商品模块
│   │   │   └── port/                #     入站端口接口
│   │   ├── domain/                   #   领域层（Entity + Repository 接口）
│   │   └── infrastructure/           #   基础设施层（JPA 实现 + JWT）
│   └── src/main/resources/
│       ├── application.yml           #   应用配置
│       └── db/migration/             #   Flyway 迁移脚本
├── frontend/                         # 前端（Vue 3 Feature-Sliced）
│   └── src/
│       ├── features/
│       │   ├── auth/                 #   认证模块（登录/注册）
│       │   ├── customer/             #   客户模块（查看/修改信息）
│       │   └── product/              #   商品模块（上架/列表）
│       ├── layouts/                  #   布局组件
│       ├── shared/                   #   共享层（API/类型/工具）
│       └── router/                   #   路由
├── http/                             # HTTP 测试文件
└── docs/                             # 设计文档
```

## 快速开始

### 环境要求

- Java 17+
- PostgreSQL 16+
- Node.js 20+

### 1. 创建数据库

```sql
CREATE DATABASE bookstore;
```

### 2. 配置数据库连接

编辑 `backend/src/main/resources/application.yml`，修改数据库用户名和密码：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bookstore
    username: your_username
    password: your_password
```

### 3. 启动后端

```bash
cd backend
./gradlew bootRun
```

后端启动在 `http://localhost:8081`

Swagger UI：`http://localhost:8081/swagger-ui.html`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:3000`

## API 概览

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册（返回 JWT） |
| POST | `/api/auth/login` | 用户登录（返回 JWT） |

### 客户

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/customers/{id}` | 查看客户信息 |
| PUT | `/api/customers/{id}` | 修改客户信息 |

### 商品

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/products` | 商品上架 |
| GET | `/api/products/{id}` | 商品详情 |
| GET | `/api/products?name=&category=` | 商品搜索 |
| PATCH | `/api/products/{id}/stock` | 库存变更 |

### 订单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders/{id}` | 订单详情 |
| GET | `/api/orders?customerId=` | 客户订单列表 |

## 运行测试

```bash
# 后端测试
cd backend && ./gradlew test

# 前端类型检查
cd frontend && npx vue-tsc --noEmit
```

## 架构设计

- **后端**：严格遵循 DDD 四层架构（接口层 → 应用层 → 领域层 → 基础设施层）
- **前端**：Feature-Sliced 模块化，每个 feature 包含自己的 API、Store、Route、Views
- **认证**：JWT 无状态认证，注册即登录
