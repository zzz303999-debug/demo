package com.example.demo.application.port;

import com.example.demo.application.customer.dto.CustomerResponse;
import com.example.demo.application.customer.dto.UpdateCustomerRequest;

/**
 * 客户入站端口 — 定义客户查询与修改用例。
 */
public interface CustomerUseCase {

    CustomerResponse findById(Long id);

    CustomerResponse updateProfile(Long id, UpdateCustomerRequest request);
}
