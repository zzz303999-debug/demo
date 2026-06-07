package com.example.demo.application.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 下单请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "客户 ID 不能为空")
    private Long customerId;

    @NotEmpty(message = "订单明细不能为空")
    @Valid
    private List<OrderItemRequest> items;
}
