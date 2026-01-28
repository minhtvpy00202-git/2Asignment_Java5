package com.poly.ASM.controller.web;

import com.poly.ASM.entity.order.Order;
import com.poly.ASM.entity.order.OrderDetail;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.product.ProductSize;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.cart.CartItem;
import com.poly.ASM.service.cart.CartService;
import com.poly.ASM.service.notification.NotificationService;
import com.poly.ASM.service.order.OrderDetailService;
import com.poly.ASM.service.order.OrderService;
import com.poly.ASM.service.product.ProductSizeService;
import com.poly.ASM.service.review.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final CartService cartService;
    private final AuthService authService;
    private final ProductSizeService productSizeService;
    private final ProductReviewService productReviewService;
    private final NotificationService notificationService;

    @GetMapping("/order/checkout")
    public String checkoutForm(Model model) {
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        return "order/check-out";
    }

    @PostMapping("/order/checkout")
    public String checkout(@RequestParam("address") String address, Model model) {
        List<CartItem> items = cartService.getCartItems();
        if (items.isEmpty()) {
            model.addAttribute("message", "Giỏ hàng trống");
            model.addAttribute("items", items);
            model.addAttribute("totalPrice", cartService.getTotalPrice());
            return "order/check-out";
        }

        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        Order order = new Order();
        order.setAccount(user);
        order.setAddress(address);
        order.setStatus("NEW");
        Order savedOrder = orderService.create(order);
        notificationService.notifyOrderPlacedForUser(user, savedOrder);
        notificationService.notifyOrderPlacedForAdmins(savedOrder);

        for (CartItem item : items) {
            if (item.getSizeId() == null) {
                continue;
            }
            Optional<ProductSize> productSize = productSizeService.findByProductIdAndSizeId(item.getProductId(), item.getSizeId());
            if (productSize.isEmpty() || productSize.get().getQuantity() < item.getQuantity()) {
                continue;
            }
            OrderDetail detail = new OrderDetail();
            Product product = new Product();
            product.setId(item.getProductId());
            detail.setProduct(product);
            detail.setOrder(savedOrder);
            detail.setPrice(item.getPrice());
            detail.setQuantity(item.getQuantity());
            detail.setSizeId(item.getSizeId());
            detail.setSizeName(item.getSizeName());
            orderDetailService.create(detail);

            ProductSize ps = productSize.get();
            ps.setQuantity(ps.getQuantity() - item.getQuantity());
            productSizeService.save(ps);
        }

        cartService.clearCart();
        return "redirect:/order/detail/" + savedOrder.getId();
    }

    @GetMapping("/order/list")
    public String list(Model model) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("orders", orderService.findByAccountUsername(user.getUsername()));
        return "order/order-list";
    }

    @GetMapping("/order/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
                || !user.getUsername().equals(orderOpt.get().getAccount().getUsername())) {
            return "redirect:/order/list";
        }

        Order order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("details", orderDetailService.findByOrderId(id));
        model.addAttribute("reviewable", isDeliveredStatus(order.getStatus()));
        model.addAttribute("reviewedProductIds", productReviewService.findReviewedProductIds(user.getUsername(), id));
        return "order/order-detail";
    }

    @GetMapping("/order/my-product-list")
    public String myProductList(Model model) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("details", orderDetailService.findByOrderAccountUsername(user.getUsername()));
        return "order/my-product-list";
    }

    private boolean isDeliveredStatus(String status) {
        if (status == null) {
            return false;
        }
        return "DELIVERED_SUCCESS".equals(status) || "DONE".equals(status);
    }
}
