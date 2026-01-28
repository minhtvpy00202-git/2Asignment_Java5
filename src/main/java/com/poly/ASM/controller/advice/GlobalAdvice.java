package com.poly.ASM.controller.advice;

import com.poly.ASM.entity.product.Category;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.entity.notification.Notification;
import com.poly.ASM.service.cart.CartService;
import com.poly.ASM.service.notification.NotificationService;
import com.poly.ASM.service.product.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalAdvice {

    private final CategoryService categoryService;
    private final AuthService authService;
    private final NotificationService notificationService;
    private final CartService cartService;

    @ModelAttribute("navCategories")
    public List<Category> navCategories() {
        return categoryService.findAll();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return authService.hasRole("ADMIN");
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount() {
        if (!authService.isAuthenticated()) {
            return 0;
        }
        return notificationService.countUnread(authService.getUser().getUsername());
    }

    @ModelAttribute("latestNotifications")
    public List<Notification> latestNotifications() {
        if (!authService.isAuthenticated()) {
            return java.util.List.of();
        }
        return notificationService.getLatest(authService.getUser().getUsername(), 8);
    }

    @ModelAttribute("cartDistinctCount")
    public long cartDistinctCount() {
        return cartService.getDistinctProductCount();
    }

    @ModelAttribute("cartProductIds")
    public java.util.Set<Integer> cartProductIds() {
        return cartService.getProductIdsInCart();
    }
}
