package com.poly.ASM.service.order;

import com.poly.ASM.entity.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findById(Long id);

    List<Order> findByAccountUsername(String username);

    Order create(Order order);

    Order update(Order order);

    void deleteById(Long id);
}
