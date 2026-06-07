package com.example.demo;

import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import com.example.demo.application.order.dto.CreateOrderRequest;
import com.example.demo.application.order.dto.OrderItemRequest;
import com.example.demo.application.order.dto.OrderResponse;
import com.example.demo.application.product.dto.CreateProductRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单 API 集成测试 — 真实 HTTP 请求测试 OrderController 所有端点。
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
class OrderApiTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private static Long customerId;
    private static Long productId1;
    private static Long productId2;
    private static Long orderId;
    private static String orderNumber;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ==================== 准备测试数据 ====================

    @Test
    @Order(1)
    void prepareCustomer() throws Exception {
        RegisterCustomerRequest body = new RegisterCustomerRequest();
        body.setUsername("orderuser");
        body.setPassword("123456");
        body.setEmail("order@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterCustomerRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/customers"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode node = objectMapper.readTree(resp.getBody());
        customerId = node.get("id").asLong();
    }

    @Test
    @Order(2)
    void prepareProduct1() throws Exception {
        CreateProductRequest body = new CreateProductRequest();
        body.setName("Java编程思想");
        body.setPrice(new BigDecimal("108.00"));
        body.setStock(200);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateProductRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode node = objectMapper.readTree(resp.getBody());
        productId1 = node.get("id").asLong();
    }

    @Test
    @Order(3)
    void prepareProduct2() throws Exception {
        CreateProductRequest body = new CreateProductRequest();
        body.setName("设计模式");
        body.setPrice(new BigDecimal("75.50"));
        body.setStock(10);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateProductRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode node = objectMapper.readTree(resp.getBody());
        productId2 = node.get("id").asLong();
    }

    // ==================== POST /api/orders ====================

    @Test
    @Order(4)
    void createOrder_ShouldReturn201() {
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(productId1);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(productId2);
        item2.setQuantity(1);

        CreateOrderRequest body = new CreateOrderRequest();
        body.setCustomerId(customerId);
        body.setItems(List.of(item1, item2));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<OrderResponse> resp = restTemplate.exchange(
                url("/api/orders"), HttpMethod.POST, req, OrderResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getOrderNumber()).startsWith("ORD");
        assertThat(resp.getBody().getCustomerId()).isEqualTo(customerId);
        assertThat(resp.getBody().getTotalAmount()).isEqualByComparingTo(new BigDecimal("291.50"));
        assertThat(resp.getBody().getItems()).hasSize(2);
        assertThat(resp.getBody().getStatus().name()).isEqualTo("PENDING");

        orderId = resp.getBody().getId();
        orderNumber = resp.getBody().getOrderNumber();
    }

    @Test
    @Order(5)
    void createOrder_EmptyItems_ShouldReturn400() {
        CreateOrderRequest body = new CreateOrderRequest();
        body.setCustomerId(customerId);
        body.setItems(List.of());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/orders"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("订单明细不能为空");
    }

    @Test
    @Order(6)
    void createOrder_NullCustomerId_ShouldReturn400() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId1);
        item.setQuantity(1);

        CreateOrderRequest body = new CreateOrderRequest();
        body.setCustomerId(null);
        body.setItems(List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/orders"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("客户 ID 不能为空");
    }

    @Test
    @Order(7)
    void createOrder_CustomerNotFound_ShouldReturn404() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId1);
        item.setQuantity(1);

        CreateOrderRequest body = new CreateOrderRequest();
        body.setCustomerId(99999L);
        body.setItems(List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/orders"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("客户不存在");
    }

    @Test
    @Order(8)
    void createOrder_ProductNotFound_ShouldReturn404() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(99999L);
        item.setQuantity(1);

        CreateOrderRequest body = new CreateOrderRequest();
        body.setCustomerId(customerId);
        body.setItems(List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/orders"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("商品不存在");
    }

    @Test
    @Order(9)
    void createOrder_InsufficientStock_ShouldReturn409() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId2);
        item.setQuantity(99999);

        CreateOrderRequest body = new CreateOrderRequest();
        body.setCustomerId(customerId);
        body.setItems(List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/orders"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).contains("库存不足");
    }

    @Test
    @Order(10)
    void createOrder_ZeroQuantity_ShouldReturn400() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId1);
        item.setQuantity(0);

        CreateOrderRequest body = new CreateOrderRequest();
        body.setCustomerId(customerId);
        body.setItems(List.of(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/orders"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("购买数量");
    }

    // ==================== GET /api/orders/{id} ====================

    @Test
    @Order(11)
    void getById_ShouldReturn200() {
        ResponseEntity<OrderResponse> resp = restTemplate.exchange(
                url("/api/orders/" + orderId), HttpMethod.GET, null, OrderResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getId()).isEqualTo(orderId);
        assertThat(resp.getBody().getOrderNumber()).isEqualTo(orderNumber);
        assertThat(resp.getBody().getItems()).hasSize(2);
    }

    @Test
    @Order(12)
    void getById_NotFound_ShouldReturn404() {
        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/orders/99999"), HttpMethod.GET, null, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("订单不存在");
    }

    // ==================== GET /api/orders/number/{orderNumber} ====================

    @Test
    @Order(13)
    void getByOrderNumber_ShouldReturn200() {
        ResponseEntity<OrderResponse> resp = restTemplate.exchange(
                url("/api/orders/number/" + orderNumber), HttpMethod.GET, null, OrderResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getOrderNumber()).isEqualTo(orderNumber);
        assertThat(resp.getBody().getId()).isEqualTo(orderId);
    }

    @Test
    @Order(14)
    void getByOrderNumber_NotFound_ShouldReturn404() {
        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/orders/number/NOT-EXIST"), HttpMethod.GET, null, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("订单不存在");
    }

    // ==================== GET /api/orders?customerId=xxx ====================

    @Test
    @Order(15)
    void getByCustomerId_ShouldReturn200() {
        ResponseEntity<OrderResponse[]> resp = restTemplate.exchange(
                url("/api/orders?customerId=" + customerId), HttpMethod.GET, null, OrderResponse[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isGreaterThanOrEqualTo(1);
        assertThat(resp.getBody()[0].getCustomerId()).isEqualTo(customerId);
    }

    @Test
    @Order(16)
    void getByCustomerId_NoOrder_ShouldReturnEmpty() {
        ResponseEntity<OrderResponse[]> resp = restTemplate.exchange(
                url("/api/orders?customerId=99999"), HttpMethod.GET, null, OrderResponse[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().length).isEqualTo(0);
    }

    // ==================== 验证下单后库存扣减 ====================

    @Test
    @Order(17)
    void verifyStockDeducted_AfterOrder() {
        // product1: 200 - 2 = 198
        ResponseEntity<String> resp1 = restTemplate.exchange(
                url("/api/products/" + productId1), HttpMethod.GET, null, String.class);
        assertThat(resp1.getBody()).contains("\"stock\":198");

        // product2: 10 - 1 = 9
        ResponseEntity<String> resp2 = restTemplate.exchange(
                url("/api/products/" + productId2), HttpMethod.GET, null, String.class);
        assertThat(resp2.getBody()).contains("\"stock\":9");
    }
}
