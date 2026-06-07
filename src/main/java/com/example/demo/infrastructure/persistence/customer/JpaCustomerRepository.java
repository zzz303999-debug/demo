package com.example.demo.infrastructure.persistence.customer;

import com.example.demo.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA 客户仓库接口。
 */
@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, Long> {
    java.util.Optional<Customer> findByUsername(String username);

    boolean existsByUsername(String username);
}
