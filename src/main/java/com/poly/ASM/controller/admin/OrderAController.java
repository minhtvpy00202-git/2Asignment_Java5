package com.poly.ASM.controller.admin;

import com.poly.ASM.entity.order.Order;
import com.poly.ASM.service.notification.NotificationService;
import com.poly.ASM.service.order.OrderDetailService;
import com.poly.ASM.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderAController {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final NotificationService notificationService;

    @GetMapping("/admin/order/index")
    public String index(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "admin/order";
    }

    @GetMapping("/admin/order/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Optional<Order> order = orderService.findById(id);
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("order", order.orElse(null));
        model.addAttribute("details", orderDetailService.findByOrderId(id));
        return "admin/order";
    }

    @GetMapping("/admin/order/delete/{id}")
    @Transactional
    public String delete(@PathVariable("id") Long id) {
        orderDetailService.deleteByOrderId(id);
        orderService.deleteById(id);
        return "redirect:/admin/order/index";
    }

    @PostMapping("/admin/order/update-status")
    @Transactional
    public String updateStatus(@RequestParam("id") Long id,
                               @RequestParam("status") String status) {
        orderService.findById(id).ifPresent(order -> {
            String previous = order.getStatus();
            order.setStatus(status);
            orderService.update(order);
            if (previous == null || !previous.equals(status)) {
                notificationService.notifyOrderStatusChange(order, status);
            }
        });
        return "redirect:/admin/order/detail/" + id;
    }
}
