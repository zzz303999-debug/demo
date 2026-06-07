package com.example.demo.infrastructure.persistence.order;

import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 订单 Repository 实现，适配 JPA 接口。
 */
@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaRepo;

    @Override
    public Order save(Order order) {
        return jpaRepo.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return jpaRepo.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return jpaRepo.findByCustomerId(customerId);
    }
}
