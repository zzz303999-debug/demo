package com.example.demo.application.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存变更请求。
 * quantity > 0 表示入库，quantity < 0 表示出库。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {

    @NotNull(message = "变更数量不能为空")
    private Integer quantity;
}
