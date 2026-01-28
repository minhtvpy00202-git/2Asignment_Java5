package com.poly.ASM.controller.web;

import com.poly.ASM.entity.notification.Notification;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final AuthService authService;
    private final NotificationService notificationService;

    @GetMapping("/notifications/read/{id}")
    public String read(@PathVariable("id") Long id) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        Optional<Notification> notificationOpt = notificationService.findByIdAndUsername(id, user.getUsername());
        if (notificationOpt.isEmpty()) {
            return "redirect:/home/index";
        }

        Notification notification = notificationOpt.get();
        if (notification.getRead() == null || !notification.getRead()) {
            notificationService.markRead(notification);
        }

        if (notification.getOrder() != null && notification.getOrder().getId() != null) {
            if (authService.hasRole("ADMIN")) {
                return "redirect:/admin/order/detail/" + notification.getOrder().getId();
            }
            return "redirect:/order/detail/" + notification.getOrder().getId();
        }

        return "redirect:/home/index";
    }
}
