package com.example.demo.infrastructure.persistence.customer;

import com.example.demo.domain.customer.Customer;
import com.example.demo.domain.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 客户 Repository 实现，适配 JPA 接口。
 */
@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final JpaCustomerRepository jpaRepo;

    @Override
    public Customer save(Customer customer) {
        return jpaRepo.save(customer);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        return jpaRepo.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepo.existsByUsername(username);
    }
}
