package com.example.demo.application.order;

import com.example.demo.application.order.dto.CreateOrderRequest;
import com.example.demo.application.order.dto.OrderResponse;
import com.example.demo.application.port.OrderUseCase;
import com.example.demo.domain.customer.CustomerRepository;
import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.OrderDomainService;
import com.example.demo.domain.order.OrderItem;
import com.example.demo.domain.order.OrderRepository;
import com.example.demo.domain.product.Product;
import com.example.demo.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单应用服务 — 实现 OrderUseCase 入站端口，协调跨聚合操作。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderApplicationService implements OrderUseCase {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderDomainService orderDomainService;

    /**
     * 创建订单（下单）。
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. 校验客户存在
        if (!customerRepository.findById(request.getCustomerId()).isPresent()) {
            throw new IllegalArgumentException("客户不存在，ID：" + request.getCustomerId());
        }

        // 2. 创建订单聚合根
        Order order = Order.create(request.getCustomerId());

        // 3. 遍历订单项，校验商品 & 扣减库存
        for (var itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "商品不存在，ID：" + itemReq.getProductId()));

            // 扣减库存（领域行为：内部校验库存充足）
            product.decreaseStock(itemReq.getQuantity());
            productRepository.save(product);

            // 创建订单明细（价格快照）
            OrderItem orderItem = OrderItem.create(
                    product.getId(), itemReq.getQuantity(), product.getPrice());
            order.addItem(orderItem);
        }

        // 4. 领域服务计算总价
        orderDomainService.calculateTotal(order);

        // 5. 持久化订单
        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    /**
     * 查询单个订单。
     */
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在，ID：" + id));
        return OrderResponse.from(order);
    }

    /**
     * 按订单号查询。
     */
    @Transactional(readOnly = true)
    public OrderResponse findByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在，订单号：" + orderNumber));
        return OrderResponse.from(order);
    }

    /**
     * 查询某客户的所有订单。
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponse::from)
                .toList();
    }
}
