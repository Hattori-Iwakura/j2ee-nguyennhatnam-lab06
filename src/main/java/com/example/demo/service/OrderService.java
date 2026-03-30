package com.example.demo.service;

import com.example.demo.model.CartItem;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(String username, List<CartItem> cartItems) {
        double totalAmount = cartItems.stream()
                .mapToDouble(CartItem::getTotal)
                .sum();

        Order order = new Order(username, LocalDateTime.now(), totalAmount);

        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail(
                    order,
                    item.getProductId(),
                    item.getProductName(),
                    item.getPrice(),
                    item.getQuantity()
            );
            order.getOrderDetails().add(detail);
        }

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsernameOrderByOrderDateDesc(username);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
}
