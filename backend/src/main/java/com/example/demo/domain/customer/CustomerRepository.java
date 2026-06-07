package com.example.demo.domain.customer;

import java.util.Optional;

/**
 * 客户 Repository 接口（领域层定义，基础设施层实现）。
 */
public interface CustomerRepository {
    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    Optional<Customer> findByUsername(String username);

    boolean existsByUsername(String username);
}
