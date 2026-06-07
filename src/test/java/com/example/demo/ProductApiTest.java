package com.example.demo;

import com.example.demo.application.product.dto.CreateProductRequest;
import com.example.demo.application.product.dto.ProductResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 商品 API 集成测试 — 真实 HTTP 请求测试 ProductController 所有端点。
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
class ProductApiTest {

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

    private static Long productId1;
    private static Long productId2;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ==================== POST /api/products ====================

    @Test
    @Order(1)
    void create_ShouldReturn201() {
        CreateProductRequest body = new CreateProductRequest();
        body.setName("深入理解Java虚拟机");
        body.setDescription("JVM 经典书籍");
        body.setPrice(new BigDecimal("79.00"));
        body.setStock(100);
        body.setCategory("计算机");
        body.setAuthor("周志明");
        body.setIsbn("978-7-111-00001");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateProductRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<ProductResponse> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.POST, req, ProductResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getName()).isEqualTo("深入理解Java虚拟机");
        assertThat(resp.getBody().getPrice()).isEqualByComparingTo(new BigDecimal("79.00"));
        assertThat(resp.getBody().getStock()).isEqualTo(100);
        productId1 = resp.getBody().getId();
    }

    @Test
    @Order(2)
    void create_SecondProduct_ShouldReturn201() {
        CreateProductRequest body = new CreateProductRequest();
        body.setName("领域驱动设计");
        body.setDescription("DDD 经典之作");
        body.setPrice(new BigDecimal("99.00"));
        body.setStock(50);
        body.setCategory("计算机");
        body.setAuthor("Eric Evans");
        body.setIsbn("978-7-111-00002");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateProductRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<ProductResponse> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.POST, req, ProductResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getName()).isEqualTo("领域驱动设计");
        productId2 = resp.getBody().getId();
    }

    @Test
    @Order(3)
    void create_BlankName_ShouldReturn400() {
        CreateProductRequest body = new CreateProductRequest();
        body.setName("");
        body.setPrice(new BigDecimal("10.00"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateProductRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("商品名称不能为空");
    }

    @Test
    @Order(4)
    void create_NullPrice_ShouldReturn400() {
        CreateProductRequest body = new CreateProductRequest();
        body.setName("测试商品");
        body.setPrice(null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateProductRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("单价不能为空");
    }

    @Test
    @Order(5)
    void create_ZeroPrice_ShouldReturn400() {
        CreateProductRequest body = new CreateProductRequest();
        body.setName("测试商品");
        body.setPrice(BigDecimal.ZERO);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateProductRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("单价必须大于 0");
    }

    @Test
    @Order(6)
    void create_NegativePrice_ShouldReturn400() {
        CreateProductRequest body = new CreateProductRequest();
        body.setName("测试商品");
        body.setPrice(new BigDecimal("-1.00"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateProductRequest> req = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.POST, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("单价");
    }

    // ==================== GET /api/products/{id} ====================

    @Test
    @Order(7)
    void getById_ShouldReturn200() {
        ResponseEntity<ProductResponse> resp = restTemplate.exchange(
                url("/api/products/" + productId1), HttpMethod.GET, null, ProductResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getId()).isEqualTo(productId1);
        assertThat(resp.getBody().getName()).isEqualTo("深入理解Java虚拟机");
        assertThat(resp.getBody().getStock()).isEqualTo(100);
    }

    @Test
    @Order(8)
    void getById_NotFound_ShouldReturn404() {
        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products/99999"), HttpMethod.GET, null, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("商品不存在");
    }

    // ==================== GET /api/products?name=xxx ====================

    @Test
    @Order(9)
    void searchByName_ShouldReturn200() {
        ResponseEntity<ProductResponse[]> resp = restTemplate.exchange(
                url("/api/products?name=Java"), HttpMethod.GET, null, ProductResponse[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isGreaterThanOrEqualTo(1);
        assertThat(resp.getBody()[0].getName()).contains("Java");
    }

    @Test
    @Order(10)
    void searchByName_NoMatch_ShouldReturnEmpty() {
        ResponseEntity<ProductResponse[]> resp = restTemplate.exchange(
                url("/api/products?name=不存在的书"), HttpMethod.GET, null, ProductResponse[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().length).isEqualTo(0);
    }

    // ==================== GET /api/products?category=xxx ====================

    @Test
    @Order(11)
    void searchByCategory_ShouldReturn200() {
        ResponseEntity<ProductResponse[]> resp = restTemplate.exchange(
                url("/api/products?category=计算机"), HttpMethod.GET, null, ProductResponse[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isGreaterThanOrEqualTo(2);
        assertThat(resp.getBody()[0].getCategory()).isEqualTo("计算机");
    }

    @Test
    @Order(12)
    void searchByCategory_NoMatch_ShouldReturnEmpty() {
        ResponseEntity<ProductResponse[]> resp = restTemplate.exchange(
                url("/api/products?category=未知分类"), HttpMethod.GET, null, ProductResponse[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().length).isEqualTo(0);
    }

    @Test
    @Order(13)
    void search_NoParams_ShouldReturnEmpty() {
        ResponseEntity<ProductResponse[]> resp = restTemplate.exchange(
                url("/api/products"), HttpMethod.GET, null, ProductResponse[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().length).isEqualTo(0);
    }

    // ==================== PATCH /api/products/{id}/stock ====================

    @Test
    @Order(14)
    void updateStock_Increase_ShouldReturn200() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>("{\"quantity\": 20}", headers);

        ResponseEntity<ProductResponse> resp = restTemplate.exchange(
                url("/api/products/" + productId1 + "/stock"),
                HttpMethod.PATCH, req, ProductResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getStock()).isEqualTo(120);
    }

    @Test
    @Order(15)
    void updateStock_Decrease_ShouldReturn200() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>("{\"quantity\": -30}", headers);

        ResponseEntity<ProductResponse> resp = restTemplate.exchange(
                url("/api/products/" + productId1 + "/stock"),
                HttpMethod.PATCH, req, ProductResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getStock()).isEqualTo(90);
    }

    @Test
    @Order(16)
    void updateStock_InsufficientStock_ShouldReturn409() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>("{\"quantity\": -99999}", headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products/" + productId1 + "/stock"),
                HttpMethod.PATCH, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).contains("库存不足");
    }

    @Test
    @Order(17)
    void updateStock_ProductNotFound_ShouldReturn404() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>("{\"quantity\": 10}", headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/api/products/99999/stock"),
                HttpMethod.PATCH, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("商品不存在");
    }
}
