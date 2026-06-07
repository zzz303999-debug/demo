# 网上书店系统 — 前后端接口文档

> **后端基址**: `http://localhost:8080`
> **内容类型**: `application/json`
> **字符编码**: UTF-8

---

## 目录

- [1. 通用约定](#1-通用约定)
- [2. 前端架构与项目结构](#2-前端架构与项目结构)
- [3. Axios 封装与基础配置](#3-axios-封装与基础配置)
- [4. 客户模块](#4-客户模块)
- [5. 商品模块](#5-商品模块)
- [6. 订单模块](#6-订单模块)
- [7. Pinia Store 设计](#7-pinia-store-设计)
- [8. TypeScript 类型定义](#8-typescript-类型定义)
- [9. 错误码对照表](#9-错误码对照表)

---

## 1. 通用约定

### 1.1 响应格式

**成功响应** — 直接返回数据对象或数组：

```json
// 单个对象
{ "id": 1, "username": "zhangsan", "email": "zhangsan@example.com", ... }

// 列表
[ { "id": 1, ... }, { "id": 2, ... } ]
```

**错误响应** — 统一结构：

```json
{
  "timestamp": "2026-06-07T10:30:00.123",
  "status": 404,
  "error": "Not Found",
  "message": "客户不存在，ID: 99999"
}
```

### 1.2 HTTP 状态码语义

| 状态码 | 含义 | 触发场景 |
|--------|------|---------|
| `200` | 成功 | GET 查询、PUT 更新、PATCH 库存变更 |
| `201` | 创建成功 | POST 注册/上架/下单 |
| `400` | 请求参数错误 | 字段校验失败（空值、格式错误、长度不符） |
| `404` | 资源不存在 | 查/改不存在的客户、商品、订单 |
| `409` | 业务冲突 | 用户名重复、库存不足 |

### 1.3 命名约定

- 所有字段使用 **camelCase**（与 Java Jackson 序列化一致，`ObjectMapper` 的 `PropertyNamingStrategies.LOWER_CAMEL_CASE` 默认行为在 Spring Boot 中取决于配置——但本项目 DTO 均为 `@Data` 注解，Jackson 默认按字段名序列化，即 camelCase 原样输出）。
  > **实际确认**：后端 DTO 字段名使用 camelCase（如 `orderNumber`、`customerId`、`totalAmount`、`unitPrice`），JSON 序列化后保持一致。
- 日期时间格式：ISO 8601（`2026-06-07T10:30:00.123`）
- 金额类型：`number`（Java `BigDecimal` → JSON `number`，如 `247.00`）

---

## 2. 前端架构与项目结构

### 2.1 技术栈

| 类别 | 选型 |
|------|------|
| 框架 | Vue 3 (Composition API + `<script setup>`) |
| UI 组件库 | Ant Design Vue 4 |
| 状态管理 | Pinia |
| HTTP 客户端 | Axios |
| 路由 | Vue Router 4 |
| 构建工具 | Vite |
| 语言 | TypeScript |

### 2.2 Feature-Sliced 目录结构

```
src/
├── shared/                          # 共享层 — 跨模块通信的契约
│   ├── api/                         #   Axios 实例、拦截器
│   │   ├── http-client.ts           #     基础 Axios 封装
│   │   └── error-handler.ts         #     统一错误处理
│   ├── types/                       #   共享 TypeScript 类型
│   │   ├── api.ts                   #     通用 API 响应类型
│   │   ├── customer.ts              #     客户类型（供多模块使用）
│   │   ├── product.ts               #     商品类型（供多模块使用）
│   │   └── order.ts                 #     订单类型（供多模块使用）
│   ├── composables/                 #   共享组合式函数
│   │   └── use-api.ts               #     通用请求封装（loading/error 管理）
│   └── utils/                       #   工具函数
│       └── format.ts                #     金额/日期格式化
│
├── features/                        # 业务功能模块
│   ├── customer/                    #   客户管理
│   │   ├── api/                     #     API 调用函数
│   │   │   └── customer-api.ts
│   │   ├── stores/                  #     Pinia stores
│   │   │   └── customer-store.ts
│   │   ├── views/                   #     页面级组件
│   │   │   ├── RegisterView.vue
│   │   │   ├── CustomerDetailView.vue
│   │   │   └── CustomerEditView.vue
│   │   ├── components/              #     模块内部组件
│   │   │   └── CustomerForm.vue
│   │   └── router.ts                #     模块路由
│   │
│   ├── product/                     #   商品管理
│   │   ├── api/
│   │   │   └── product-api.ts
│   │   ├── stores/
│   │   │   └── product-store.ts
│   │   ├── views/
│   │   │   ├── ProductCreateView.vue
│   │   │   ├── ProductListView.vue
│   │   │   └── ProductDetailView.vue
│   │   ├── components/
│   │   │   ├── ProductCard.vue
│   │   │   └── StockChanger.vue
│   │   └── router.ts
│   │
│   └── order/                       #   订单管理
│       ├── api/
│       │   └── order-api.ts
│       ├── stores/
│       │   └── order-store.ts
│       ├── views/
│       │   ├── OrderCreateView.vue
│       │   ├── OrderDetailView.vue
│       │   └── OrderListView.vue
│       ├── components/
│       │   └── OrderItemList.vue
│       └── router.ts
│
├── layouts/                         # 布局组件
│   └── DefaultLayout.vue
│
├── router/                          # 根路由聚合
│   └── index.ts
│
├── App.vue
└── main.ts
```

### 2.3 模块间通信规则

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│ customer │     │ product  │     │  order   │
│ feature  │     │ feature  │     │ feature  │
└────┬─────┘     └────┬─────┘     └────┬─────┘
     │                │                │
     └──────────┬─────┘────────────────┘
                │
          ┌─────┴──────┐
          │   shared/   │  ← 所有模块通过 shared 层通信
          │  types/api  │    不直接 import 其他 feature
          └────────────┘
```

**原则**：
- 每个 feature 的 `api/` 只 import `shared/api/http-client` 和 `shared/types/`
- Store 之间通过 shared types 传递数据，不互相引用 store
- 若订单页需要展示客户名称 → 从 `shared/types/customer` 取类型，由父组件（页面）组合数据

---

## 3. Axios 封装与基础配置

### 3.1 HTTP 客户端 (`shared/api/http-client.ts`)

```typescript
import axios from 'axios'
import { message } from 'ant-design-vue'
import type { ApiError } from '@/shared/types/api'

const httpClient = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 响应拦截器 — 统一错误处理
httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const err = error.response.data as ApiError
      const status = error.response.status

      // 409 业务冲突交给调用方自行处理（如展示库存不足提示）
      if (status === 409) {
        return Promise.reject(error)
      }

      // 其他错误统一 toast 提示
      const msg = err?.message ?? `请求失败 (${status})`
      message.error(msg)
      return Promise.reject(error)
    }

    // 网络错误
    message.error('网络异常，请检查连接')
    return Promise.reject(error)
  },
)

export default httpClient
```

### 3.2 通用组合式函数 (`shared/composables/use-api.ts`)

```typescript
import { ref } from 'vue'
import type { AxiosResponse } from 'axios'

/**
 * 封装 API 调用的 loading / error 状态管理。
 *
 * 用法:
 *   const { loading, error, execute } = useApi()
 *   const data = await execute(() => productApi.create(payload))
 */
export function useApi<T = unknown>() {
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function execute(fn: () => Promise<AxiosResponse<T>>): Promise<T | null> {
    loading.value = true
    error.value = null
    try {
      const res = await fn()
      return res.data
    } catch (e: any) {
      // 409 类错误由调用方处理，不在此处吞掉
      if (e?.response?.status === 409) {
        error.value = e.response.data?.message ?? '操作冲突'
        throw e
      }
      error.value = e?.response?.data?.message ?? '请求失败'
      return null
    } finally {
      loading.value = false
    }
  }

  return { loading, error, execute }
}
```

---

## 4. 客户模块

### 4.1 客户注册

```
POST /api/customers
```

**Request Body:**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `username` | `string` | ✅ | 3–50 字符，全局唯一 | 用户名 |
| `password` | `string` | ✅ | 6–100 字符 | 密码（明文传输，后端 BCrypt 加密存储） |
| `email` | `string` | ❌ | 合法邮箱格式 | 邮箱 |
| `phone` | `string` | ❌ | — | 手机号 |
| `address` | `string` | ❌ | — | 地址 |

```json
{
  "username": "zhangsan",
  "password": "123456",
  "email": "zhangsan@example.com",
  "phone": "13800001111",
  "address": "北京市海淀区"
}
```

**Response** `201 Created`:

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `number` | 客户 ID |
| `username` | `string` | 用户名 |
| `email` | `string` | 邮箱 |
| `phone` | `string` | 手机号 |
| `address` | `string` | 地址 |
| `createdAt` | `string` (ISO 8601) | 注册时间 |

```json
{
  "id": 1,
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "phone": "13800001111",
  "address": "北京市海淀区",
  "createdAt": "2026-06-07T10:30:00.123"
}
```

**错误场景:**

| 状态码 | 条件 | message 示例 |
|--------|------|-------------|
| `400` | username 为空 | `username: 用户名不能为空` |
| `400` | username < 3 字符 | `username: 用户名长度 3-50 字符` |
| `400` | password 为空 | `password: 密码不能为空` |
| `400` | password < 6 字符 | `password: 密码长度 6-100 字符` |
| `400` | email 格式错误 | `email: 邮箱格式不正确` |
| `409` | 用户名已存在 | `用户名已存在: zhangsan` |

---

### 4.2 查询客户信息

```
GET /api/customers/{id}
```

**Path Parameters:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `number` | 客户 ID |

**Response** `200 OK` — 结构同 [4.1 客户注册响应](#41-客户注册)。

**错误场景:**

| 状态码 | 条件 | message 示例 |
|--------|------|-------------|
| `404` | ID 不存在 | `客户不存在，ID: 99999` |

---

### 4.3 修改客户信息

```
PUT /api/customers/{id}
```

**Path Parameters:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `number` | 客户 ID |

**Request Body**（所有字段均为可选项，只传需要修改的字段）:

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `email` | `string` | ❌ | 合法邮箱格式 | 新邮箱 |
| `phone` | `string` | ❌ | — | 新手机号 |
| `address` | `string` | ❌ | — | 新地址 |

```json
{
  "email": "newemail@example.com",
  "phone": "13900002222",
  "address": "上海市浦东新区"
}
```

> **注意**：用户名和密码不可通过此接口修改，仅支持邮箱、手机号、地址。

**Response** `200 OK` — 更新后的完整客户信息，结构同 [4.1](#41-客户注册)。

**错误场景:**

| 状态码 | 条件 | message 示例 |
|--------|------|-------------|
| `400` | email 格式错误 | `email: 邮箱格式不正确` |
| `404` | ID 不存在 | `客户不存在，ID: 99999` |

---

### 4.4 前端 API 层 (`features/customer/api/customer-api.ts`)

```typescript
import httpClient from '@/shared/api/http-client'
import type { Customer, RegisterForm, UpdateCustomerForm } from '@/shared/types/customer'

export const customerApi = {
  /** POST /api/customers — 注册 */
  register(data: RegisterForm) {
    return httpClient.post<Customer>('/api/customers', data)
  },

  /** GET /api/customers/{id} — 查询 */
  getById(id: number) {
    return httpClient.get<Customer>(`/api/customers/${id}`)
  },

  /** PUT /api/customers/{id} — 修改 */
  update(id: number, data: UpdateCustomerForm) {
    return httpClient.put<Customer>(`/api/customers/${id}`, data)
  },
}
```

---

## 5. 商品模块

### 5.1 商品上架

```
POST /api/products
```

**Request Body:**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `name` | `string` | ✅ | 最长 200 字符 | 商品名称 |
| `description` | `string` | ❌ | — | 商品描述 |
| `price` | `number` | ✅ | > 0 | 单价 |
| `stock` | `number` | ❌ | >= 0，默认 0 | 初始库存 |
| `category` | `string` | ❌ | — | 商品分类 |
| `author` | `string` | ❌ | — | 作者（图书特有） |
| `isbn` | `string` | ❌ | — | ISBN（图书特有） |

```json
{
  "name": "深入理解Java虚拟机",
  "description": "JVM经典书籍，深入剖析Java虚拟机原理",
  "price": 79.00,
  "stock": 100,
  "category": "计算机",
  "author": "周志明",
  "isbn": "978-7-111-50001"
}
```

**Response** `201 Created`:

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `number` | 商品 ID |
| `name` | `string` | 商品名称 |
| `description` | `string` | 商品描述 |
| `price` | `number` | 单价 |
| `stock` | `number` | 当前库存 |
| `category` | `string` | 商品分类 |
| `author` | `string` | 作者 |
| `isbn` | `string` | ISBN |
| `createdAt` | `string` (ISO 8601) | 创建时间 |

```json
{
  "id": 1,
  "name": "深入理解Java虚拟机",
  "description": "JVM经典书籍，深入剖析Java虚拟机原理",
  "price": 79.00,
  "stock": 100,
  "category": "计算机",
  "author": "周志明",
  "isbn": "978-7-111-50001",
  "createdAt": "2026-06-07T10:30:00.123"
}
```

**错误场景:**

| 状态码 | 条件 | message 示例 |
|--------|------|-------------|
| `400` | name 为空 | `name: 商品名称不能为空` |
| `400` | price 为 null | `price: 单价不能为空` |
| `400` | price <= 0 | `price: 单价必须大于 0` |
| `400` | stock < 0 | `stock: 库存不能为负数` |

---

### 5.2 查询商品详情

```
GET /api/products/{id}
```

**Response** `200 OK` — 结构同 [5.1](#51-商品上架)。

**错误场景:**

| 状态码 | 条件 | message 示例 |
|--------|------|-------------|
| `404` | ID 不存在 | `商品不存在，ID: 99999` |

---

### 5.3 商品搜索

```
GET /api/products?name={keyword}&category={category}
```

**Query Parameters:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | `string` | ❌ | 按名称模糊搜索 |
| `category` | `string` | ❌ | 按分类精确匹配 |

**说明**：
- `name` 和 `category` 二选一，同时传时 **name 优先**
- 两个参数都不传 → 返回 **空数组** `[]`

**Response** `200 OK`:

```json
[
  {
    "id": 1,
    "name": "深入理解Java虚拟机",
    "price": 79.00,
    "stock": 100,
    "category": "计算机",
    "author": "周志明",
    ...
  },
  {
    "id": 2,
    "name": "Java并发编程实战",
    "price": 89.00,
    "stock": 200,
    "category": "计算机",
    "author": "Brian Goetz",
    ...
  }
]
```

---

### 5.4 库存变更

```
PATCH /api/products/{id}/stock
```

> **注意**：此接口使用 `PATCH` 方法，非 `PUT`！`quantity > 0` 表示入库，`< 0` 表示出库。

**Path Parameters:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `number` | 商品 ID |

**Request Body:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `quantity` | `number` | ✅ | 变更量，正数入库，负数出库 |

```json
// 入库 20 件
{ "quantity": 20 }

// 出库 30 件
{ "quantity": -30 }
```

**Response** `200 OK` — 更新后的商品信息，结构同 [5.1](#51-商品上架)。

**错误场景:**

| 状态码 | 条件 | message 示例 |
|--------|------|-------------|
| `400` | quantity 为 null | `quantity: 变更数量不能为空` |
| `404` | 商品不存在 | `商品不存在，ID: 99999` |
| `409` | 库存不足（出库超出） | `库存不足，当前库存: 10，需要出库: 99999` |

---

### 5.5 前端 API 层 (`features/product/api/product-api.ts`)

```typescript
import httpClient from '@/shared/api/http-client'
import type { Product, CreateProductForm, UpdateStockForm } from '@/shared/types/product'

export const productApi = {
  /** POST /api/products — 上架 */
  create(data: CreateProductForm) {
    return httpClient.post<Product>('/api/products', data)
  },

  /** GET /api/products/{id} — 详情 */
  getById(id: number) {
    return httpClient.get<Product>(`/api/products/${id}`)
  },

  /** GET /api/products?name=&category= — 搜索 */
  search(params?: { name?: string; category?: string }) {
    return httpClient.get<Product[]>('/api/products', { params })
  },

  /** PATCH /api/products/{id}/stock — 库存变更 */
  updateStock(id: number, data: UpdateStockForm) {
    return httpClient.patch<Product>(`/api/products/${id}/stock`, data)
  },
}
```

---

## 6. 订单模块

### 6.1 创建订单

```
POST /api/orders
```

**Request Body:**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `customerId` | `number` | ✅ | 客户 ID |
| `items` | `array` | ✅ | 订单明细，至少 1 项 |

**items 数组元素:**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `productId` | `number` | ✅ | — | 商品 ID |
| `quantity` | `number` | ✅ | > 0 | 购买数量 |

```json
{
  "customerId": 1,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 2, "quantity": 1 }
  ]
}
```

**Response** `201 Created`:

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `number` | 订单 ID |
| `orderNumber` | `string` | 订单号（格式：`ORD` + 时间戳 + 4位随机数） |
| `customerId` | `number` | 客户 ID |
| `totalAmount` | `number` | 订单总金额（所有明细 subtotal 之和） |
| `status` | `"PENDING"` | 订单状态（初始为 PENDING） |
| `items` | `array` | 订单明细列表 |
| `createdAt` | `string` (ISO 8601) | 创建时间 |

**items 数组元素:**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `number` | 明细 ID |
| `productId` | `number` | 商品 ID |
| `quantity` | `number` | 购买数量 |
| `unitPrice` | `number` | 下单时商品单价（快照） |
| `subtotal` | `number` | 小计 = unitPrice × quantity |

```json
{
  "id": 1,
  "orderNumber": "ORD17181234560001",
  "customerId": 1,
  "totalAmount": 247.00,
  "status": "PENDING",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "unitPrice": 89.00,
      "subtotal": 178.00
    },
    {
      "id": 2,
      "productId": 2,
      "quantity": 1,
      "unitPrice": 69.00,
      "subtotal": 69.00
    }
  ],
  "createdAt": "2026-06-07T10:30:00.123"
}
```

**业务逻辑**：
1. 校验客户存在
2. 校验所有商品存在且库存充足
3. `unitPrice` 取下单瞬间商品价格（快照，后续商品调价不影响历史订单）
4. 下单成功 → 自动扣减对应商品库存
5. 初始状态 `PENDING`

**错误场景:**

| 状态码 | 条件 | message 示例 |
|--------|------|-------------|
| `400` | items 为空数组 | `items: 订单明细不能为空` |
| `400` | customerId 为 null | `customerId: 客户 ID 不能为空` |
| `400` | quantity 为 0 | `quantity: 购买数量必须大于 0` |
| `404` | 客户不存在 | `客户不存在，ID: 99999` |
| `404` | 商品不存在 | `商品不存在，ID: 99999` |
| `409` | 库存不足 | `商品 [Java并发编程实战] 库存不足，需要 99999，当前库存 200` |

---

### 6.2 按 ID 查询订单

```
GET /api/orders/{id}
```

**Response** `200 OK` — 结构同 [6.1](#61-创建订单)。

**错误场景:** `404` — 订单不存在。

---

### 6.3 按订单号查询

```
GET /api/orders/number/{orderNumber}
```

**Path Parameters:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `orderNumber` | `string` | 订单号（如 `ORD17181234560001`） |

**Response** `200 OK` — 结构同 [6.1](#61-创建订单)。

**错误场景:** `404` — 订单不存在。

---

### 6.4 按客户 ID 查询订单列表

```
GET /api/orders?customerId={customerId}
```

**Query Parameters:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `customerId` | `number` | ✅ | 客户 ID |

**Response** `200 OK` — `Order[]` 数组，无订单时返回 `[]`。

---

### 6.5 OrderStatus 枚举

| 值 | 显示名 | 说明 |
|----|--------|------|
| `PENDING` | 待处理 | 订单初始状态 |
| `PAID` | 已支付 | 支付完成 |
| `SHIPPED` | 已发货 | 商品已发出 |
| `CANCELLED` | 已取消 | 订单已取消 |

---

### 6.6 前端 API 层 (`features/order/api/order-api.ts`)

```typescript
import httpClient from '@/shared/api/http-client'
import type { Order, CreateOrderForm } from '@/shared/types/order'

export const orderApi = {
  /** POST /api/orders — 创建订单 */
  create(data: CreateOrderForm) {
    return httpClient.post<Order>('/api/orders', data)
  },

  /** GET /api/orders/{id} — 按 ID 查询 */
  getById(id: number) {
    return httpClient.get<Order>(`/api/orders/${id}`)
  },

  /** GET /api/orders/number/{orderNumber} — 按订单号查询 */
  getByOrderNumber(orderNumber: string) {
    return httpClient.get<Order>(`/api/orders/number/${orderNumber}`)
  },

  /** GET /api/orders?customerId= — 按客户 ID 查询列表 */
  getByCustomerId(customerId: number) {
    return httpClient.get<Order[]>('/api/orders', { params: { customerId } })
  },
}
```

---

## 7. Pinia Store 设计

### 7.1 客户 Store (`features/customer/stores/customer-store.ts`)

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { customerApi } from '../api/customer-api'
import type { Customer, RegisterForm, UpdateCustomerForm } from '@/shared/types/customer'

export const useCustomerStore = defineStore('customer', () => {
  const currentCustomer = ref<Customer | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function register(form: RegisterForm) {
    loading.value = true
    error.value = null
    try {
      const res = await customerApi.register(form)
      currentCustomer.value = res.data
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '注册失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchCustomer(id: number) {
    loading.value = true
    error.value = null
    try {
      const res = await customerApi.getById(id)
      currentCustomer.value = res.data
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '查询失败'
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateProfile(id: number, form: UpdateCustomerForm) {
    loading.value = true
    error.value = null
    try {
      const res = await customerApi.update(id, form)
      currentCustomer.value = res.data
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '修改失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  return { currentCustomer, loading, error, register, fetchCustomer, updateProfile }
})
```

### 7.2 商品 Store (`features/product/stores/product-store.ts`)

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { productApi } from '../api/product-api'
import type { Product, CreateProductForm, UpdateStockForm } from '@/shared/types/product'

export const useProductStore = defineStore('product', () => {
  const products = ref<Product[]>([])
  const currentProduct = ref<Product | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function create(form: CreateProductForm) {
    loading.value = true
    try {
      const res = await productApi.create(form)
      products.value.unshift(res.data)
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '上架失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchProduct(id: number) {
    loading.value = true
    try {
      const res = await productApi.getById(id)
      currentProduct.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function search(params?: { name?: string; category?: string }) {
    loading.value = true
    try {
      const res = await productApi.search(params)
      products.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  /** quantity > 0 入库，< 0 出库 */
  async function updateStock(id: number, form: UpdateStockForm) {
    loading.value = true
    try {
      const res = await productApi.updateStock(id, form)
      // 同步更新列表中的数据
      const idx = products.value.findIndex((p) => p.id === id)
      if (idx !== -1) products.value[idx] = res.data
      if (currentProduct.value?.id === id) currentProduct.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  return { products, currentProduct, loading, error, create, fetchProduct, search, updateStock }
})
```

### 7.3 订单 Store (`features/order/stores/order-store.ts`)

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { orderApi } from '../api/order-api'
import type { Order, CreateOrderForm } from '@/shared/types/order'

export const useOrderStore = defineStore('order', () => {
  const currentOrder = ref<Order | null>(null)
  const orderList = ref<Order[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function createOrder(form: CreateOrderForm) {
    loading.value = true
    error.value = null
    try {
      const res = await orderApi.create(form)
      currentOrder.value = res.data
      orderList.value.unshift(res.data)
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '下单失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchOrder(id: number) {
    loading.value = true
    try {
      const res = await orderApi.getById(id)
      currentOrder.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchByOrderNumber(orderNumber: string) {
    loading.value = true
    try {
      const res = await orderApi.getByOrderNumber(orderNumber)
      currentOrder.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchByCustomerId(customerId: number) {
    loading.value = true
    try {
      const res = await orderApi.getByCustomerId(customerId)
      orderList.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  return {
    currentOrder, orderList, loading, error,
    createOrder, fetchOrder, fetchByOrderNumber, fetchByCustomerId,
  }
})
```

---

## 8. TypeScript 类型定义

> 所有类型统一放在 `src/shared/types/` 下，供各 feature 模块引用。

### 8.1 通用类型 (`shared/types/api.ts`)

```typescript
/** 后端统一错误响应 */
export interface ApiError {
  timestamp: string   // ISO 8601
  status: number
  error: string       // e.g. "Not Found"
  message: string     // 详细错误描述
}

/** 订单状态枚举 */
export type OrderStatus = 'PENDING' | 'PAID' | 'SHIPPED' | 'CANCELLED'

/** OrderStatus 中文映射 */
export const OrderStatusLabel: Record<OrderStatus, string> = {
  PENDING: '待处理',
  PAID: '已支付',
  SHIPPED: '已发货',
  CANCELLED: '已取消',
}
```

### 8.2 客户类型 (`shared/types/customer.ts`)

```typescript
/** 客户信息（响应） */
export interface Customer {
  id: number
  username: string
  email: string
  phone: string
  address: string
  createdAt: string  // ISO 8601
}

/** 注册表单 */
export interface RegisterForm {
  username: string
  password: string
  email?: string
  phone?: string
  address?: string
}

/** 修改信息表单（所有字段可选） */
export interface UpdateCustomerForm {
  email?: string
  phone?: string
  address?: string
}
```

### 8.3 商品类型 (`shared/types/product.ts`)

```typescript
/** 商品信息（响应） */
export interface Product {
  id: number
  name: string
  description: string
  price: number
  stock: number
  category: string
  author: string
  isbn: string
  createdAt: string  // ISO 8601
}

/** 上架表单 */
export interface CreateProductForm {
  name: string
  description?: string
  price: number
  stock?: number
  category?: string
  author?: string
  isbn?: string
}

/** 库存变更表单 */
export interface UpdateStockForm {
  quantity: number   // > 0 入库，< 0 出库
}
```

### 8.4 订单类型 (`shared/types/order.ts`)

```typescript
import type { OrderStatus } from './api'

/** 订单明细项（响应） */
export interface OrderItemResponse {
  id: number
  productId: number
  quantity: number
  unitPrice: number   // 下单时单价快照
  subtotal: number    // unitPrice × quantity
}

/** 订单（响应） */
export interface Order {
  id: number
  orderNumber: string
  customerId: number
  totalAmount: number
  status: OrderStatus
  items: OrderItemResponse[]
  createdAt: string  // ISO 8601
}

/** 下单请求中的明细项 */
export interface OrderItemRequest {
  productId: number
  quantity: number
}

/** 下单表单 */
export interface CreateOrderForm {
  customerId: number
  items: OrderItemRequest[]
}
```

---

## 9. 错误码对照表

| HTTP 状态码 | 含义 | 前端处理策略 |
|------------|------|-------------|
| `200` | 成功 | 正常展示数据 |
| `201` | 创建成功 | 展示创建结果 / 跳转详情页 |
| `400` | 参数校验失败 | 在对应表单字段下方展示 `message` 中的错误信息；可解析 `field: message` 格式精确绑定到表单项 |
| `404` | 资源不存在 | 展示 404 页面或 `a-empty` 空状态；列表场景可忽略 |
| `409` | 业务冲突 | **按场景处理**：库存不足 → 弹窗提示用户调整数量；用户名重复 → 表单字段提示 |
| 网络错误 | Axios 无 response | toast "网络异常，请检查连接" |

### 9.1 409 冲突的精确处理示例

```typescript
// 下单时的 409 处理
try {
  await orderApi.create(form)
  message.success('下单成功')
} catch (e: any) {
  if (e.response?.status === 409) {
    // 库存不足 → 弹窗提示，不清空表单
    message.warning(e.response.data.message)
  } else if (e.response?.status === 400) {
    // 参数错误 → 解析并绑定到表单
    const fieldErrors = parseFieldErrors(e.response.data.message)
    formRef.value?.setFields(
      fieldErrors.map((f) => ({ name: f.field, errors: [f.message] }))
    )
  }
  // 其他错误由拦截器统一 toast
}
```

---

## 附录 A：API 速查表

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 客户 | `POST` | `/api/customers` | 注册 |
| 客户 | `GET` | `/api/customers/{id}` | 查询客户 |
| 客户 | `PUT` | `/api/customers/{id}` | 修改信息 |
| 商品 | `POST` | `/api/products` | 上架 |
| 商品 | `GET` | `/api/products/{id}` | 查询商品 |
| 商品 | `GET` | `/api/products?name=&category=` | 搜索 |
| 商品 | `PATCH` | `/api/products/{id}/stock` | 库存变更 |
| 订单 | `POST` | `/api/orders` | 创建订单 |
| 订单 | `GET` | `/api/orders/{id}` | 按 ID 查询 |
| 订单 | `GET` | `/api/orders/number/{orderNumber}` | 按订单号查询 |
| 订单 | `GET` | `/api/orders?customerId=` | 客户订单列表 |

## 附录 B：业务流程图（Happy Path）

```
1. POST /api/customers        → 注册客户，获得 customerId
2. POST /api/products         → 上架商品，获得 productId（可多次）
3. POST /api/orders           → 下单（关联 customerId + productId[]）
4. GET  /api/orders/{id}      → 查订单
5. GET  /api/orders?customerId= → 查客户所有订单
6. GET  /api/products/{id}    → 验证库存已扣减
7. PUT  /api/customers/{id}   → 修改客户信息
```
