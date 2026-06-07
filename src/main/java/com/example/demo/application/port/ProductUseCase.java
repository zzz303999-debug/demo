package com.example.demo.application.port;

import com.example.demo.application.product.dto.CreateProductRequest;
import com.example.demo.application.product.dto.ProductResponse;
import com.example.demo.application.product.dto.UpdateStockRequest;

import java.util.List;

/**
 * 商品入站端口 — 定义商品相关的用例。
 */
public interface ProductUseCase {

    ProductResponse create(CreateProductRequest request);

    ProductResponse updateStock(Long productId, UpdateStockRequest request);

    List<ProductResponse> searchByName(String keyword);

    List<ProductResponse> findByCategory(String category);

    ProductResponse findById(Long id);
}
