package com.example.demo.domain.order;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 订单领域服务 — 处理跨聚合的领域逻辑。
 */
@Component
public class OrderDomainService {

    /**
     * 校验并计算订单总价。
     * 遍历明细，累加 subtotal 得到总价。
     */
    public BigDecimal calculateTotal(Order order) {
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("订单必须包含至少一个商品");
        }
        order.recalculateTotal();
        return order.getTotalAmount();
    }
}
