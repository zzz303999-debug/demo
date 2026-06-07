package com.example.demo.domain.common;

/**
 * 订单状态枚举。
 */
public enum OrderStatus {
    PENDING("待处理"),
    PAID("已支付"),
    SHIPPED("已发货"),
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
