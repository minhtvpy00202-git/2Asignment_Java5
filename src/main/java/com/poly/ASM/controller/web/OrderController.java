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
import com.poly.ASM.service.payment.PayosPaymentService;
import com.poly.ASM.service.product.ProductSizeService;
import com.poly.ASM.service.review.ProductReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final PayosPaymentService payosPaymentService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/order/checkout")
    public String checkoutForm(Model model) {
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        return "order/check-out";
    }

    @PostMapping("/order/checkout")
    public String checkout(@RequestParam("address") String address,
                           @RequestParam(value = "lat", required = false) Double lat,
                           @RequestParam(value = "lng", required = false) Double lng,
                           @RequestParam("paymentMethod") String paymentMethod,
                           Model model) {
        List<CartItem> items = cartService.getCartItems();
        if (items.isEmpty()) {
            model.addAttribute("message", "Giỏ hàng trống");
            model.addAttribute("items", items);
            model.addAttribute("totalPrice", cartService.getTotalPrice());
            return "order/check-out";
        }
        for (CartItem item : items) {
            if (item.getSizeId() == null) {
                model.addAttribute("message", "Vui lòng chọn size trước khi đặt hàng.");
                model.addAttribute("items", items);
                model.addAttribute("totalPrice", cartService.getTotalPrice());
                return "order/check-out";
            }
            Optional<ProductSize> productSize = productSizeService.findByProductIdAndSizeId(item.getProductId(), item.getSizeId());
            if (productSize.isEmpty() || productSize.get().getQuantity() == null) {
                model.addAttribute("message", "Sản phẩm trong giỏ hàng không còn tồn kho.");
                model.addAttribute("items", items);
                model.addAttribute("totalPrice", cartService.getTotalPrice());
                return "order/check-out";
            }
            Integer stock = productSize.get().getQuantity();
            Integer qty = item.getQuantity();
            if (qty == null || qty <= 0 || qty > stock) {
                String name = item.getName() != null ? item.getName() : "Sản phẩm";
                String size = item.getSizeName() != null ? item.getSizeName() : "size đã chọn";
                model.addAttribute("message", name + " (" + size + ") vượt quá tồn kho. Vui lòng giảm số lượng.");
                model.addAttribute("items", items);
                model.addAttribute("totalPrice", cartService.getTotalPrice());
                return "order/check-out";
            }
        }

        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        Order order = new Order();
        order.setAccount(user);
        order.setAddress(address);
        String initialStatus = "PLACED_UNPAID";
        if ("BANK".equalsIgnoreCase(paymentMethod)) {
            initialStatus = "PLACED_UNPAID";
        }
        order.setStatus(initialStatus);
        Order savedOrder = orderService.create(order);
        updateOrderCoordinates(savedOrder.getId(), lat, lng);
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
        if ("BANK".equalsIgnoreCase(paymentMethod)) {
            return "redirect:/order/bank-transfer/" + savedOrder.getId();
        }
        return "redirect:/order/detail/" + savedOrder.getId();
    }

    @GetMapping("/order/bank-transfer/{id}")
    public String bankTransfer(@PathVariable("id") Long id,
                               @RequestParam(value = "message", required = false) String message,
                               HttpServletRequest request,
                               Model model) {
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
        List<OrderDetail> details = orderDetailService.findByOrderId(id);
        BigDecimal total = calculateOrderTotal(details);
        model.addAttribute("order", order);
        model.addAttribute("totalPrice", total);
        if (message != null && !message.isBlank()) {
            model.addAttribute("message", message);
        }
        long amount = toPayosAmount(total);
        if (amount <= 0) {
            model.addAttribute("message", "Số tiền thanh toán không hợp lệ");
            return "order/bank-transfer";
        }
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request).replacePath(null).build().toUriString();
        String returnUrl = baseUrl + "/order/payos/return?orderId=" + order.getId();
        String cancelUrl = baseUrl + "/order/payos/cancel?orderId=" + order.getId();
        try {
            CreatePaymentLinkResponse response = payosPaymentService.createPaymentLink(
                    order.getId(),
                    amount,
                    "Thanh toan don hang #" + order.getId(),
                    returnUrl,
                    cancelUrl
            );
            model.addAttribute("checkoutUrl", response.getCheckoutUrl());
            String qrCode = response.getQrCode();
            if (qrCode != null && !qrCode.isBlank()) {
                model.addAttribute("qrImageSrc", buildQrImageSrc(qrCode));
            }
            model.addAttribute("accountName", response.getAccountName());
            model.addAttribute("accountNumber", response.getAccountNumber());
            model.addAttribute("bankName", "BIDV");
            model.addAttribute("bankBin", response.getBin());
            model.addAttribute("paymentLinkId", response.getPaymentLinkId());
        } catch (PayOSException ex) {
            model.addAttribute("message", "Không thể tạo link thanh toán. Vui lòng thử lại.");
        }
        return "order/bank-transfer";
    }

    @PostMapping("/order/bank-transfer/confirm")
    public String confirmBankTransfer(@RequestParam("orderId") Long orderId) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
                || !user.getUsername().equals(orderOpt.get().getAccount().getUsername())) {
            return "redirect:/order/list";
        }
        Order order = orderOpt.get();
        try {
            PaymentLink paymentLink = payosPaymentService.getPaymentLink(orderId);
            if (paymentLink != null && paymentLink.getStatus() != null) {
                PaymentLinkStatus status = paymentLink.getStatus();
                if (status == PaymentLinkStatus.PAID) {
                    updateOrderStatusIfChanged(order, "PLACED_PAID");
                    return "redirect:/order/detail/" + orderId;
                }
            }
        } catch (PayOSException ignored) {
        }
        String redirectUrl = UriComponentsBuilder.fromPath("/order/bank-transfer/" + orderId)
                .queryParam("message", "Thanh toán chưa hoàn tất")
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/order/bank-transfer/cancel")
    public String cancelBankTransfer(@RequestParam("orderId") Long orderId) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
                || !user.getUsername().equals(orderOpt.get().getAccount().getUsername())) {
            return "redirect:/order/list";
        }
        String redirectUrl = UriComponentsBuilder.fromPath("/order/bank-transfer/cancel")
                .queryParam("orderId", orderId)
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/order/bank-transfer/cancel")
    public String cancelBankTransferPage(@RequestParam("orderId") Long orderId, Model model) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
                || !user.getUsername().equals(orderOpt.get().getAccount().getUsername())) {
            return "redirect:/order/list";
        }
        model.addAttribute("order", orderOpt.get());
        model.addAttribute("showCancelPrompt", true);
        return "order/bank-transfer";
    }

    @PostMapping("/order/bank-transfer/cancel/switch-cod")
    public String switchToCodAfterCancel(@RequestParam("orderId") Long orderId) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
                || !user.getUsername().equals(orderOpt.get().getAccount().getUsername())) {
            return "redirect:/order/list";
        }
        try {
            payosPaymentService.cancelPaymentLink(orderId, "Switch to COD");
        } catch (PayOSException ignored) {
        }
        updateOrderStatusIfChanged(orderOpt.get(), "PLACED_UNPAID");
        return "redirect:/order/detail/" + orderId;
    }

    @PostMapping("/order/bank-transfer/cancel/delete")
    @Transactional
    public String cancelAndDeleteOrder(@RequestParam("orderId") Long orderId) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
                || !user.getUsername().equals(orderOpt.get().getAccount().getUsername())) {
            return "redirect:/order/list";
        }
        try {
            payosPaymentService.cancelPaymentLink(orderId, "Cancel order");
        } catch (PayOSException ignored) {
        }
        notificationService.deleteByOrderId(orderId);
        orderDetailService.deleteByOrderId(orderId);
        orderService.deleteById(orderId);
        return "redirect:/order/list";
    }

    @GetMapping("/order/payos/return")
    public String payosReturn(@RequestParam("orderId") Long orderId) {
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/order/list";
        }
        Order order = orderOpt.get();
        try {
            PaymentLink paymentLink = payosPaymentService.getPaymentLink(orderId);
            if (paymentLink != null && paymentLink.getStatus() != null) {
                PaymentLinkStatus status = paymentLink.getStatus();
                if (status == PaymentLinkStatus.PAID) {
                    updateOrderStatusIfChanged(order, "PLACED_PAID");
                    return "redirect:/order/detail/" + orderId;
                }
            }
        } catch (PayOSException ignored) {
        }
        String redirectUrl = UriComponentsBuilder.fromPath("/order/bank-transfer/" + orderId)
                .queryParam("message", "Thanh toán chưa hoàn tất")
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/order/payos/cancel")
    public String payosCancel(@RequestParam("orderId") Long orderId) {
        String redirectUrl = UriComponentsBuilder.fromPath("/order/bank-transfer/cancel")
                .queryParam("orderId", orderId)
                .build()
                .toUriString();
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/order/payos/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> payosStatus(@RequestParam("orderId") Long orderId) {
        Account user = authService.getUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
                || !user.getUsername().equals(orderOpt.get().getAccount().getUsername())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentLink paymentLink = payosPaymentService.getPaymentLink(orderId);
            if (paymentLink != null && paymentLink.getStatus() != null) {
                PaymentLinkStatus status = paymentLink.getStatus();
                response.put("status", status.getValue());
                if (status == PaymentLinkStatus.PAID) {
                    updateOrderStatusIfChanged(orderOpt.get(), "PLACED_PAID");
                    response.put("redirectUrl", "/order/detail/" + orderId);
                }
            }
        } catch (PayOSException ex) {
            response.put("message", "PayOS error");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payos/webhook")
    @ResponseBody
    public ResponseEntity<String> payosWebhook(@RequestBody Webhook webhook) {
        try {
            WebhookData data = payosPaymentService.verifyWebhook(webhook);
            if (data == null || data.getOrderCode() == null) {
                return ResponseEntity.badRequest().body("Invalid webhook");
            }
            PaymentLink paymentLink = payosPaymentService.getPaymentLink(data.getOrderCode());
            if (paymentLink != null && paymentLink.getStatus() != null) {
                applyPaymentStatus(data.getOrderCode(), paymentLink.getStatus());
            }
            return ResponseEntity.ok("OK");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Invalid webhook");
        }
    }

    private BigDecimal calculateOrderTotal(List<OrderDetail> details) {
        BigDecimal total = BigDecimal.ZERO;
        if (details == null) {
            return total;
        }
        for (OrderDetail detail : details) {
            if (detail.getPrice() == null || detail.getQuantity() == null) {
                continue;
            }
            total = total.add(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
        }
        return total;
    }

    private long toPayosAmount(BigDecimal total) {
        if (total == null) {
            return 0;
        }
        return total.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private String buildQrImageSrc(String qrCode) {
        if (qrCode == null || qrCode.isBlank()) {
            return null;
        }
        if (qrCode.startsWith("data:")) {
            return qrCode;
        }
        if (qrCode.startsWith("iVBOR") || qrCode.startsWith("/9j/")) {
            return "data:image/png;base64," + qrCode;
        }
        String encoded = URLEncoder.encode(qrCode, StandardCharsets.UTF_8);
        return "https://api.qrserver.com/v1/create-qr-code/?size=260x260&data=" + encoded;
    }

    private void applyPaymentStatus(Long orderId, PaymentLinkStatus status) {
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) {
            return;
        }
        Order order = orderOpt.get();
        if (status == PaymentLinkStatus.PAID) {
            updateOrderStatusIfChanged(order, "PLACED_PAID");
        }
    }

    private void updateOrderStatusIfChanged(Order order, String status) {
        if (order == null || status == null) {
            return;
        }
        String previous = order.getStatus();
        if (previous != null && previous.equals(status)) {
            return;
        }
        order.setStatus(status);
        orderService.update(order);
        notificationService.notifyOrderStatusChange(order, status);
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

    private void updateOrderCoordinates(Long orderId, Double lat, Double lng) {
        if (orderId == null || lat == null || lng == null) {
            return;
        }
        Optional<ColumnPair> columns = findLatLngColumns(orderId);
        if (columns.isEmpty()) {
            return;
        }
        ColumnPair pair = columns.get();
        try {
            jdbcTemplate.update("update orders set " + pair.lat + " = ?, " + pair.lng + " = ? where id = ?",
                    lat, lng, orderId);
        } catch (Exception ignored) {
        }
    }

    private Optional<ColumnPair> findLatLngColumns(Long orderId) {
        try {
            return jdbcTemplate.query("select * from orders where id = ?", rs -> {
                if (!rs.next()) {
                    return Optional.empty();
                }
                ResultSetMetaData meta = rs.getMetaData();
                String latCol = null;
                String lngCol = null;
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String label = meta.getColumnLabel(i);
                    String columnName = meta.getColumnName(i);
                    String key = (label == null || label.isBlank() ? columnName : label).toLowerCase(Locale.ROOT);
                    if (latCol == null && (key.equals("lat") || key.equals("latitude") || key.equals("order_lat") || key.equals("order_latitude") || key.equals("shipping_lat") || key.equals("delivery_lat") || key.equals("ship_lat"))) {
                        latCol = columnName;
                    }
                    if (lngCol == null && (key.equals("lng") || key.equals("longitude") || key.equals("order_lng") || key.equals("order_longitude") || key.equals("shipping_lng") || key.equals("delivery_lng") || key.equals("ship_lng"))) {
                        lngCol = columnName;
                    }
                }
                if (latCol == null || lngCol == null) {
                    return Optional.empty();
                }
                return Optional.of(new ColumnPair(latCol, lngCol));
            }, orderId);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static class ColumnPair {
        private final String lat;
        private final String lng;

        private ColumnPair(String lat, String lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }
}
