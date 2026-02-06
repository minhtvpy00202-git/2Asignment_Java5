package com.poly.ASM.controller.admin;

import com.poly.ASM.entity.order.Order;
import com.poly.ASM.service.notification.NotificationService;
import com.poly.ASM.service.order.OrderDetailService;
import com.poly.ASM.service.order.OrderService;
import com.poly.ASM.service.payment.PayosPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.payos.exception.PayOSException;

import java.sql.ResultSetMetaData;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderAController {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final NotificationService notificationService;
    private final JdbcTemplate jdbcTemplate;
    private final PayosPaymentService payosPaymentService;

    @GetMapping("/admin/order/index")
    public String index(@RequestParam(value = "payosMessage", required = false) String payosMessage,
                        @RequestParam(value = "payosError", required = false) Boolean payosError,
                        Model model) {
        model.addAttribute("orders", orderService.findAll());
        if (payosMessage != null && !payosMessage.isBlank()) {
            model.addAttribute("payosMessage", payosMessage);
        }
        model.addAttribute("payosError", payosError != null && payosError);
        return "admin/order";
    }

    @GetMapping("/admin/order/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Optional<Order> order = orderService.findById(id);
        Order current = order.orElse(null);
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("order", current);
        model.addAttribute("details", orderDetailService.findByOrderId(id));
        if (current != null) {
            Optional<double[]> coords = findOrderCoordinates(current.getId());
            coords.ifPresent(values -> {
                model.addAttribute("deliveryLat", values[0]);
                model.addAttribute("deliveryLng", values[1]);
            });
        }
        return "admin/order";
    }

    @GetMapping("/admin/order/delete/{id}")
    @Transactional
    public String delete(@PathVariable("id") Long id) {
        notificationService.deleteByOrderId(id);
        orderDetailService.deleteByOrderId(id);
        orderService.deleteById(id);
        return "redirect:/admin/order/index";
    }

    @PostMapping("/admin/order/update-status")
    @Transactional
    public Object updateStatus(@RequestParam("id") Long id,
                               @RequestParam("status") String status,
                               @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        orderService.findById(id).ifPresent(order -> {
            String previous = order.getStatus();
            order.setStatus(status);
            orderService.update(order);
            if (previous == null || !previous.equals(status)) {
                notificationService.notifyOrderStatusChange(order, status);
            }
        });
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return ResponseEntity.noContent().build();
        }
        return "redirect:/admin/order/detail/" + id;
    }

    @PostMapping("/admin/order/payos/cancel")
    public String cancelPayos(@RequestParam("orderCode") Long orderCode,
                              RedirectAttributes redirectAttributes) {
        String message;
        boolean error = false;
        if (orderCode == null || orderCode <= 0) {
            message = "Mã đơn không ở trạng thái đang chờ thanh toán. Xin thử lại";
            error = true;
        } else {
            boolean cancelled = false;
            try {
                cancelled = payosPaymentService.cancelIfPending(orderCode, "Admin cancel pending order");
            } catch (PayOSException ignored) {
                cancelled = false;
            }
            if (cancelled) {
                message = "Đã gửi lệnh huỷ lên PayOS cho đơn #" + orderCode;
            } else {
                message = "Mã đơn không ở trạng thái đang chờ thanh toán. Xin thử lại";
                error = true;
            }
        }
        redirectAttributes.addAttribute("payosMessage", message);
        redirectAttributes.addAttribute("payosError", error);
        return "redirect:/admin/order/index";
    }

    private Optional<double[]> findOrderCoordinates(Long orderId) {
        try {
            return jdbcTemplate.query("select * from orders where id = ?", rs -> {
                if (!rs.next()) {
                    return Optional.empty();
                }
                ResultSetMetaData meta = rs.getMetaData();
                Integer latIndex = null;
                Integer lngIndex = null;
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String name = meta.getColumnLabel(i);
                    if (name == null || name.isBlank()) {
                        name = meta.getColumnName(i);
                    }
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    String key = name.toLowerCase(Locale.ROOT);
                    if (latIndex == null && (key.equals("lat") || key.equals("latitude") || key.equals("order_lat") || key.equals("order_latitude") || key.equals("shipping_lat") || key.equals("delivery_lat") || key.equals("ship_lat"))) {
                        latIndex = i;
                    }
                    if (lngIndex == null && (key.equals("lng") || key.equals("longitude") || key.equals("order_lng") || key.equals("order_longitude") || key.equals("shipping_lng") || key.equals("delivery_lng") || key.equals("ship_lng"))) {
                        lngIndex = i;
                    }
                }
                if (latIndex == null || lngIndex == null) {
                    return Optional.empty();
                }
                Object latObj = rs.getObject(latIndex);
                Object lngObj = rs.getObject(lngIndex);
                if (!(latObj instanceof Number) || !(lngObj instanceof Number)) {
                    return Optional.empty();
                }
                double lat = ((Number) latObj).doubleValue();
                double lng = ((Number) lngObj).doubleValue();
                return Optional.of(new double[]{lat, lng});
            }, orderId);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
