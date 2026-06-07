package com.example.demo.infrastructure.persistence.product;

import com.example.demo.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA 商品仓库接口。
 */
@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContaining(String keyword);

    List<Product> findByCategory(String category);
}
