package com.example.demo.application.port;

import com.example.demo.application.customer.dto.CustomerResponse;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import com.example.demo.application.customer.dto.UpdateCustomerRequest;

/**
 * 客户入站端口 — 定义客户相关的用例。
 */
public interface CustomerUseCase {

    CustomerResponse register(RegisterCustomerRequest request);

    CustomerResponse findById(Long id);

    CustomerResponse updateProfile(Long id, UpdateCustomerRequest request);
}
