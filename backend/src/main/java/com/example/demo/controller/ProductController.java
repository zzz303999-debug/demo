package com.example.demo.controller;

import com.example.demo.application.port.ProductUseCase;
import com.example.demo.application.product.dto.CreateProductRequest;
import com.example.demo.application.product.dto.ProductResponse;
import com.example.demo.application.product.dto.UpdateStockRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品 Web 适配器 — 将 HTTP 请求适配到 ProductUseCase 入站端口。
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return productUseCase.create(request);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productUseCase.findById(id);
    }

    @GetMapping
    public List<ProductResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        if (name != null && !name.isBlank()) {
            return productUseCase.searchByName(name);
        }
        if (category != null && !category.isBlank()) {
            return productUseCase.findByCategory(category);
        }
        return List.of();
    }

    @PatchMapping("/{id}/stock")
    public ProductResponse updateStock(@PathVariable Long id,
                                       @Valid @RequestBody UpdateStockRequest request) {
        return productUseCase.updateStock(id, request);
    }
}
