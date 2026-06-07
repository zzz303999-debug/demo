package com.example.demo.application.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品上架请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称最长 200 字符")
    private String name;

    private String description;

    @NotNull(message = "单价不能为空")
    @Positive(message = "单价必须大于 0")
    private BigDecimal price;

    @Positive(message = "库存不能为负数")
    private int stock;

    private String category;

    private String author;

    private String isbn;
}
