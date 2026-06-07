package com.example.demo.domain.order;

import com.example.demo.domain.common.BaseEntity;
import com.example.demo.domain.common.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单聚合根。
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /**
     * 订单明细 — 由 Order 聚合根管理。
     * cascade = ALL：对 OrderItem 的所有操作随 Order 一起持久化。
     * orphanRemoval = true：从集合中移除的 OrderItem 会被自动删除。
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ========== 领域行为 ==========

    /**
     * 创建订单（静态工厂方法）。
     */
    public static Order create(Long customerId) {
        Order order = new Order();
        order.orderNumber = generateOrderNumber();
        order.customerId = customerId;
        order.totalAmount = BigDecimal.ZERO;
        order.status = OrderStatus.PENDING;
        order.items = new ArrayList<>();
        return order;
    }

    /**
     * 添加订单明细，并重新计算总价。
     */
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
        recalculateTotal();
    }

    /**
     * 重新计算订单总金额。
     */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 生成唯一订单号。
     */
    private static String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis()
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    // ========== 仅供 JPA / 框架调用的 setter ==========
    void setId(Long id) { this.id = id; }
}
