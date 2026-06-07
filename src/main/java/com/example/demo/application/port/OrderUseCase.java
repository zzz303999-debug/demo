package com.example.demo.application.port;

import com.example.demo.application.order.dto.CreateOrderRequest;
import com.example.demo.application.order.dto.OrderResponse;

import java.util.List;

/**
 * 订单入站端口 — 定义订单相关的用例。
 */
public interface OrderUseCase {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse findById(Long id);

    OrderResponse findByOrderNumber(String orderNumber);

    List<OrderResponse> findByCustomerId(Long customerId);
}
