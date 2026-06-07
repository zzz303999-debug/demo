package com.example.demo.domain.product;

import java.util.Optional;

/**
 * 商品 Repository 接口（领域层定义，基础设施层实现）。
 */
public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(Long id);

    java.util.List<Product> findByNameContaining(String keyword);

    java.util.List<Product> findByCategory(String category);
}
