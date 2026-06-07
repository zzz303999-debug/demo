package com.example.demo.infrastructure.persistence.product;

import com.example.demo.domain.product.Product;
import com.example.demo.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 商品 Repository 实现，适配 JPA 接口。
 */
@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaRepo;

    @Override
    public Product save(Product product) {
        return jpaRepo.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<Product> findByNameContaining(String keyword) {
        return jpaRepo.findByNameContaining(keyword);
    }

    @Override
    public List<Product> findByCategory(String category) {
        return jpaRepo.findByCategory(category);
    }
}
