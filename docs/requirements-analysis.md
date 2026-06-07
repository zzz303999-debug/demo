# 网上书店系统 - 需求分析文档

## 1. 项目概述

### 1.1 项目名称
网上书店管理系统（Online Bookstore Management System）

### 1.2 项目背景
构建一个基于 Spring Boot 的网上书店后端系统，支持客户管理、图书商品管理、订单管理等核心业务功能。

### 1.3 技术栈
| 层次 | 技术选型 |
|------|---------|
| 框架 | Spring Boot 4.0.6 |
| 构建工具 | Gradle 9.5 |
| 数据库 | PostgreSQL |
| ORM | Spring Data JPA (Hibernate) |
| 数据库迁移 | Flyway |
| 参数校验 | Bean Validation (Hibernate Validator) |
| 简化代码 | Lombok |
| JSON 序列化 | Jackson（Spring Boot 内置） |

### 1.4 架构设计
严格遵循 DDD（领域驱动设计）四层架构：

```
┌─────────────────────────────────────────┐
│         Interfaces 接口层                │
│  Controller（REST API）                  │
├─────────────────────────────────────────┤
│         Application 应用层               │
│  ApplicationService（应用服务）          │
│  DTO（数据传输对象）                     │
│  事务控制                               │
├─────────────────────────────────────────┤
│         Domain 领域层                    │
│  Entity（实体）                          │
│  Aggregate（聚合根）                     │
│  DomainService（领域服务）               │
│  Repository 接口                         │
├─────────────────────────────────────────┤
│         Infrastructure 基础设施层        │
│  JPA Repository 实现                     │
│  数据库配置                              │
└─────────────────────────────────────────┘
```

---

## 2. 功能需求

### 2.1 客户模块（Customer）

#### 2.1.1 客户注册
- **需求描述**：新用户通过提供必要信息完成注册
- **输入字段**：
  - 用户名（username）：必填，唯一，3-50 字符
  - 密码（password）：必填，6-100 字符，加密存储
  - 邮箱（email）：选填，需符合邮箱格式
  - 手机号（phone）：选填，需符合手机号格式
  - 地址（address）：选填
- **业务规则**：
  - 用户名全局唯一，重复注册返回错误提示
  - 密码使用 BCrypt 加密存储
  - 注册成功后返回客户基本信息（不含密码）

#### 2.1.2 查看客户信息
- **需求描述**：客户可查看自己的注册信息
- **输入**：客户 ID
- **输出**：客户基本信息（用户名、邮箱、手机号、地址、注册时间）
- **业务规则**：不返回密码

#### 2.1.3 修改客户信息
- **需求描述**：客户可修改自己的部分信息
- **可修改字段**：邮箱、手机号、地址
- **不可修改字段**：用户名、密码（密码修改单独处理）
- **业务规则**：修改后更新 `updated_at` 时间戳

---

### 2.2 商品模块（Product）

#### 2.2.1 商品上架
- **需求描述**：管理员可添加新图书商品到系统
- **输入字段**：
  - 商品名称（name）：必填，最多 200 字符
  - 商品描述（description）：选填
  - 单价（price）：必填，大于 0，保留 2 位小数
  - 库存数量（stock）：必填，不小于 0
  - 分类（category）：选填
  - 作者（author）：图书特有，选填
  - ISBN（isbn）：图书特有，选填
- **业务规则**：商品名称 + ISBN 组合唯一（若有 ISBN）

#### 2.2.2 库存管理
- **需求描述**：对商品库存进行增加/减少操作
- **操作类型**：
  - 入库（增加库存）
  - 出库（减少库存，需校验库存充足）
- **业务规则**：减少库存时，库存不能为负数

#### 2.2.3 商品查询
- **需求描述**：支持多条件查询商品
- **查询条件**：商品名称（模糊搜索）、分类、作者
- **输出**：商品列表，分页返回

---

### 2.3 订单模块（Order）

#### 2.3.1 下单
- **需求描述**：客户选择商品并创建订单
- **输入字段**：
  - 客户 ID
  - 订单明细列表（每条包含：商品 ID、购买数量）
- **业务规则**：
  - 系统自动生成唯一订单号（格式：`ORD` + 时间戳 + 随机数）
  - 校验所有商品是否存在且库存充足
  - 每项明细的 `unit_price` 取下单时的商品价格（快照）
  - 每项明细的 `subtotal = unit_price × quantity`
  - 订单 `total_amount = 所有明细 subtotal 之和`
  - 下单成功后扣减商品库存
  - 初始状态为 `PENDING`

#### 2.3.2 查询订单
- **需求描述**：支持按客户 ID 或订单号查询订单
- **输出**：订单基本信息 + 订单明细列表

#### 2.3.3 计算总价
- **需求描述**：计算订单的总金额
- **实现方式**：遍历订单明细，累加 `subtotal`
- **说明**：此功能内嵌在下单流程中，由领域服务负责

---

## 3. 数据库设计

### 3.1 ER 图（实体关系描述）

```
┌──────────┐       ┌──────────────┐       ┌──────────┐
│ Customer │       │    Order     │       │ Product  │
├──────────┤       ├──────────────┤       ├──────────┤
│ id (PK)  │───<   │ id (PK)      │       │ id (PK)  │──┐
│ username │       │ order_number │       │ name     │  │
│ password │       │ customer_id  │       │ price    │  │
│ email    │       │ total_amount │       │ stock    │  │
│ phone    │       │ status       │       │ ...      │  │
│ address  │       │ created_at   │       └──────────┘  │
└──────────┘       │ updated_at   │                      │
                   └──────────────┘                      │
                          │                              │
                          │ 1:N                          │
                          ▼                              │
                   ┌──────────────┐                      │
                   │  OrderItem   │                      │
                   ├──────────────┤                      │
                   │ id (PK)      │                      │
                   │ order_id(FK) │──────────────────────┘
                   │ product_id   │──────> Product.id
                   │ quantity     │
                   │ unit_price   │
                   │ subtotal     │
                   └──────────────┘
```

### 3.2 表结构

#### customer（客户表）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键，自增 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt 加密） |
| email | VARCHAR(100) | - | 邮箱 |
| phone | VARCHAR(20) | - | 手机号 |
| address | VARCHAR(255) | - | 地址 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新时间 |

#### product（商品表）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键，自增 |
| name | VARCHAR(200) | NOT NULL | 商品名称 |
| description | TEXT | - | 商品描述 |
| price | DECIMAL(10,2) | NOT NULL, CHECK > 0 | 单价 |
| stock | INT | NOT NULL, DEFAULT 0, CHECK >= 0 | 库存数量 |
| category | VARCHAR(50) | - | 商品分类 |
| author | VARCHAR(100) | - | 作者（图书） |
| isbn | VARCHAR(20) | - | ISBN（图书） |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新时间 |

#### orders（订单表）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键，自增 |
| order_number | VARCHAR(50) | NOT NULL, UNIQUE | 订单号 |
| customer_id | BIGINT | NOT NULL, FK → customer.id | 客户 ID |
| total_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | 订单总金额 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 订单状态 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新时间 |

#### order_item（订单明细表）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键，自增 |
| order_id | BIGINT | NOT NULL, FK → orders.id | 订单 ID |
| product_id | BIGINT | NOT NULL, FK → product.id | 商品 ID |
| quantity | INT | NOT NULL, CHECK > 0 | 购买数量 |
| unit_price | DECIMAL(10,2) | NOT NULL | 单价快照 |
| subtotal | DECIMAL(12,2) | NOT NULL | 小计金额 |

### 3.3 索引设计
| 索引名 | 表 | 字段 | 用途 |
|--------|-----|------|------|
| idx_orders_customer_id | orders | customer_id | 按客户查询订单 |
| idx_orders_order_number | orders | order_number | 按订单号查询 |
| idx_order_item_order_id | order_item | order_id | 按订单查明细 |

---

## 4. API 设计概要

### 4.1 客户模块 API
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/customers/register | 客户注册 |
| GET | /api/customers/{id} | 查看客户信息 |
| PUT | /api/customers/{id} | 修改客户信息 |

### 4.2 商品模块 API
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/products | 商品上架 |
| PATCH | /api/products/{id}/stock | 库存管理 |
| GET | /api/products | 商品查询 |

### 4.3 订单模块 API
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/orders | 创建订单 |
| GET | /api/orders/{id} | 订单详情 |
| GET | /api/orders/customer/{customerId} | 客户订单列表 |
