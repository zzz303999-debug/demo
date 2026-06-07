package com.example.demo.application.customer;

import com.example.demo.application.customer.dto.CustomerResponse;
import com.example.demo.application.customer.dto.UpdateCustomerRequest;
import com.example.demo.application.port.CustomerUseCase;
import com.example.demo.domain.customer.Customer;
import com.example.demo.domain.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户应用服务 — 实现 CustomerUseCase 入站端口，管理事务边界。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerApplicationService implements CustomerUseCase {

    private final CustomerRepository customerRepository;

    /**
     * 查看客户信息。
     */
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("客户不存在，ID：" + id));
        return CustomerResponse.from(customer);
    }

    /**
     * 修改客户信息（仅邮箱、手机号、地址）。
     */
    public CustomerResponse updateProfile(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("客户不存在，ID：" + id));

        customer.updateProfile(request.getEmail(), request.getPhone(), request.getAddress());
        Customer saved = customerRepository.save(customer);
        return CustomerResponse.from(saved);
    }
}
