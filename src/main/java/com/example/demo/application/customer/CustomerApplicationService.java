package com.example.demo.application.customer;

import com.example.demo.application.customer.dto.CustomerResponse;
import com.example.demo.application.customer.dto.RegisterCustomerRequest;
import com.example.demo.application.customer.dto.UpdateCustomerRequest;
import com.example.demo.application.port.CustomerUseCase;
import com.example.demo.domain.customer.Customer;
import com.example.demo.domain.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户应用服务 — 实现 CustomerUseCase 入站端口，协调领域对象，管理事务边界。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerApplicationService implements CustomerUseCase {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 客户注册。
     */
    public CustomerResponse register(RegisterCustomerRequest request) {
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("用户名已存在：" + request.getUsername());
        }

        Customer customer = Customer.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        Customer saved = customerRepository.save(customer);
        return CustomerResponse.from(saved);
    }

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
