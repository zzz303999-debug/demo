package com.example.demo.application.product;

import com.example.demo.application.port.ProductUseCase;
import com.example.demo.application.product.dto.CreateProductRequest;
import com.example.demo.application.product.dto.ProductResponse;
import com.example.demo.application.product.dto.UpdateStockRequest;
import com.example.demo.domain.product.Product;
import com.example.demo.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品应用服务 — 实现 ProductUseCase 入站端口。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductApplicationService implements ProductUseCase {

    private final ProductRepository productRepository;

    /**
     * 商品上架。
     */
    public ProductResponse create(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .build();

        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    /**
     * 库存变更：正数入库，负数出库。
     */
    public ProductResponse updateStock(Long productId, UpdateStockRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在，ID：" + productId));

        int quantity = request.getQuantity();
        if (quantity > 0) {
            product.increaseStock(quantity);
        } else if (quantity < 0) {
            product.decreaseStock(-quantity);
        }

        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    /**
     * 按名称模糊查询商品。
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String keyword) {
        return productRepository.findByNameContaining(keyword).stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * 按分类查询商品。
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * 查看单个商品。
     */
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在，ID：" + id));
        return ProductResponse.from(product);
    }
}
