package com.poly.ASM.controller.web;

import com.poly.ASM.dto.order.OrderDTO;
import com.poly.ASM.dto.order.OrderDetailDTO;
import com.poly.ASM.dto.order.OrderItemRequestDTO;
import com.poly.ASM.dto.order.OrderRequestDTO;
import com.poly.ASM.entity.order.Order;
import com.poly.ASM.entity.order.OrderDetail;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.service.order.OrderDetailService;
import com.poly.ASM.service.order.OrderService;
import com.poly.ASM.service.product.ProductService;
import com.poly.ASM.service.user.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final ProductService productService;
    private final AccountService accountService;

    @GetMapping
    public List<OrderDTO> findAll() {
        return orderService.findAll().stream()
                .map(order -> toDto(order, orderDetailService.findByOrderId(order.getId())))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> findById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(order -> ResponseEntity.ok(toDto(order, orderDetailService.findByOrderId(order.getId()))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody OrderRequestDTO request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required");
        }
        Optional<Account> account = accountService.findByUsername(request.getUsername());
        if (account.isEmpty()) {
            return ResponseEntity.badRequest().body("Account not found");
        }

        List<OrderDetail> details = buildDetails(request.getItems());
        if (details == null) {
            return ResponseEntity.badRequest().body("Invalid order items");
        }

        Order order = new Order();
        order.setAccount(account.get());
        order.setAddress(request.getAddress());
        order.setStatus(request.getStatus());

        Order savedOrder = orderService.create(order);

        List<OrderDetail> savedDetails = saveDetails(savedOrder, details);
        return ResponseEntity.ok(toDto(savedOrder, savedDetails));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody OrderRequestDTO request) {
        Optional<Order> existing = orderService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order order = existing.get();

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            Optional<Account> account = accountService.findByUsername(request.getUsername());
            if (account.isEmpty()) {
                return ResponseEntity.badRequest().body("Account not found");
            }
            order.setAccount(account.get());
        }
        if (request.getAddress() != null) {
            order.setAddress(request.getAddress());
        }
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        Order savedOrder = orderService.update(order);

        List<OrderDetail> savedDetails = orderDetailService.findByOrderId(savedOrder.getId());
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<OrderDetail> newDetails = buildDetails(request.getItems());
            if (newDetails == null) {
                return ResponseEntity.badRequest().body("Invalid order items");
            }
            orderDetailService.deleteByOrderId(savedOrder.getId());
            savedDetails = saveDetails(savedOrder, newDetails);
        }

        return ResponseEntity.ok(toDto(savedOrder, savedDetails));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderDetailService.deleteByOrderId(id);
        orderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private List<OrderDetail> buildDetails(List<OrderItemRequestDTO> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        List<OrderDetail> details = new ArrayList<>();
        for (OrderItemRequestDTO item : items) {
            if (item.getProductId() == null) {
                return null;
            }
            Optional<Product> productOpt = productService.findById(item.getProductId());
            if (productOpt.isEmpty()) {
                return null;
            }
            Product product = productOpt.get();
            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity() != null ? item.getQuantity() : 1);
            detail.setPrice(item.getPrice() != null ? item.getPrice() : product.getPrice());
            details.add(detail);
        }
        return details;
    }

    private List<OrderDetail> saveDetails(Order order, List<OrderDetail> details) {
        List<OrderDetail> savedDetails = new ArrayList<>();
        for (OrderDetail detail : details) {
            detail.setOrder(order);
            savedDetails.add(orderDetailService.create(detail));
        }
        return savedDetails;
    }

    private OrderDTO toDto(Order order, List<OrderDetail> details) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setAddress(order.getAddress());
        dto.setStatus(order.getStatus());
        dto.setCreateDate(order.getCreateDate());
        if (order.getAccount() != null) {
            dto.setUsername(order.getAccount().getUsername());
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderDetailDTO> itemDtos = new ArrayList<>();
        for (OrderDetail detail : details) {
            OrderDetailDTO itemDto = new OrderDetailDTO();
            itemDto.setId(detail.getId());
            if (detail.getProduct() != null) {
                itemDto.setProductId(detail.getProduct().getId());
                itemDto.setProductName(detail.getProduct().getName());
            }
            itemDto.setPrice(detail.getPrice());
            itemDto.setQuantity(detail.getQuantity());
            itemDtos.add(itemDto);

            if (detail.getPrice() != null && detail.getQuantity() != null) {
                total = total.add(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            }
        }
        dto.setItems(itemDtos);
        dto.setTotalAmount(total);
        return dto;
    }
}
