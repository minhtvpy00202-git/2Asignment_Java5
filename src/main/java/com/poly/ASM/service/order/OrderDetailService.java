package com.poly.ASM.service.order;

import com.poly.ASM.entity.order.OrderDetail;

import java.util.List;
import java.util.Optional;

public interface OrderDetailService {

    List<OrderDetail> findAll();

    Optional<OrderDetail> findById(Long id);

    List<OrderDetail> findByOrderId(Long orderId);

    OrderDetail create(OrderDetail orderDetail);

    OrderDetail update(OrderDetail orderDetail);

    void deleteById(Long id);

    void deleteByOrderId(Long orderId);

    List<OrderDetail> findByOrderAccountUsername(String username);
}
