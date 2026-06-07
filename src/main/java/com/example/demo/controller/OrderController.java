package com.example.demo.controller;

import com.example.demo.application.order.dto.CreateOrderRequest;
import com.example.demo.application.order.dto.OrderResponse;
import com.example.demo.application.port.OrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单 Web 适配器 — 将 HTTP 请求适配到 OrderUseCase 入站端口。
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderUseCase.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderUseCase.findById(id);
    }

    @GetMapping("/number/{orderNumber}")
    public OrderResponse getByOrderNumber(@PathVariable String orderNumber) {
        return orderUseCase.findByOrderNumber(orderNumber);
    }

    @GetMapping
    public List<OrderResponse> getByCustomerId(@RequestParam Long customerId) {
        return orderUseCase.findByCustomerId(customerId);
    }
}
