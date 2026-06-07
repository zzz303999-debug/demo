package com.example.demo.application.customer.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改客户信息请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String address;
}
