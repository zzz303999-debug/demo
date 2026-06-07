package com.example.demo.domain.product;

import com.example.demo.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 商品实体。
 */
@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(length = 50)
    private String category;

    @Column(length = 100)
    private String author;

    @Column(length = 20)
    private String isbn;

    // ========== 领域行为 ==========

    /**
     * 增加库存。
     */
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("入库数量必须大于 0");
        }
        this.stock += quantity;
    }

    /**
     * 减少库存（出库）。
     */
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("出库数量必须大于 0");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException(
                String.format("库存不足：当前库存 %d，需要出库 %d", this.stock, quantity));
        }
        this.stock -= quantity;
    }
}
