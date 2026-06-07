package com.example.demo.application.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单商品项请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    @Positive(message = "购买数量必须大于 0")
    private int quantity;
}
