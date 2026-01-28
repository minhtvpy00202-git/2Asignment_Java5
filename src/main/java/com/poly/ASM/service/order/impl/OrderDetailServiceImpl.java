package com.poly.ASM.service.order.impl;

import com.poly.ASM.entity.order.OrderDetail;
import com.poly.ASM.repository.order.OrderDetailRepository;
import com.poly.ASM.service.order.OrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;

    @Override
    public List<OrderDetail> findAll() {
        return orderDetailRepository.findAll();
    }

    @Override
    public Optional<OrderDetail> findById(Long id) {
        return orderDetailRepository.findById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    @Override
    public OrderDetail create(OrderDetail orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail update(OrderDetail orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public void deleteById(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public void deleteByOrderId(Long orderId) {
        orderDetailRepository.deleteByOrderId(orderId);
    }

    @Override
    public List<OrderDetail> findByOrderAccountUsername(String username) {
        return orderDetailRepository.findByOrderAccountUsername(username);
    }
}
