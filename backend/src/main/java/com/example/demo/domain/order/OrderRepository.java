package com.example.demo.domain.order;

import java.util.Optional;

/**
 * 订单 Repository 接口（领域层定义，基础设施层实现）。
 */
public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNumber(String orderNumber);

    java.util.List<Order> findByCustomerId(Long customerId);
}
