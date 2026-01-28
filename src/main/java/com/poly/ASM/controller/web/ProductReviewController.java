package com.poly.ASM.controller.web;

import com.poly.ASM.entity.order.Order;
import com.poly.ASM.entity.order.OrderDetail;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.order.OrderDetailService;
import com.poly.ASM.service.order.OrderService;
import com.poly.ASM.service.review.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProductReviewController {

    private final AuthService authService;
    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final ProductReviewService productReviewService;

    @PostMapping("/order/review")
    public String create(@RequestParam("orderId") Long orderId,
                         @RequestParam("productId") Integer productId,
                         @RequestParam("starRating") Integer starRating,
                         @RequestParam(value = "reviewContent", required = false) String reviewContent,
                         @RequestParam(value = "images", required = false) MultipartFile[] images,
                         RedirectAttributes redirectAttributes) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || orderOpt.get().getAccount() == null
                || !user.getUsername().equals(orderOpt.get().getAccount().getUsername())) {
            redirectAttributes.addFlashAttribute("reviewMessage", "Đơn hàng không hợp lệ.");
            return "redirect:/order/detail/" + orderId;
        }

        Order order = orderOpt.get();
        if (!isDeliveredStatus(order.getStatus())) {
            redirectAttributes.addFlashAttribute("reviewMessage", "Chỉ được đánh giá khi đơn hàng giao thành công.");
            return "redirect:/order/detail/" + orderId;
        }

        boolean hasProduct = orderDetailService.findByOrderId(orderId)
                .stream()
                .map(OrderDetail::getProduct)
                .anyMatch(product -> product != null && productId.equals(product.getId()));
        if (!hasProduct) {
            redirectAttributes.addFlashAttribute("reviewMessage", "Sản phẩm không thuộc đơn hàng.");
            return "redirect:/order/detail/" + orderId;
        }

        if (productReviewService.hasReviewed(user.getUsername(), productId, orderId)) {
            redirectAttributes.addFlashAttribute("reviewMessage", "Bạn đã đánh giá sản phẩm này rồi.");
            return "redirect:/order/detail/" + orderId;
        }

        if (starRating == null || starRating < 1 || starRating > 5) {
            redirectAttributes.addFlashAttribute("reviewMessage", "Vui lòng chọn số sao hợp lệ.");
            return "redirect:/order/detail/" + orderId;
        }

        List<String> imageNames = saveReviewImages(images);
        Product product = new Product();
        product.setId(productId);
        productReviewService.createReview(user, product, order, starRating, reviewContent, imageNames);
        redirectAttributes.addFlashAttribute("reviewMessage", "Đã gửi đánh giá.");
        return "redirect:/order/detail/" + orderId;
    }

    private boolean isDeliveredStatus(String status) {
        return "DELIVERED_SUCCESS".equals(status) || "DONE".equals(status);
    }

    private List<String> saveReviewImages(MultipartFile[] files) {
        List<String> results = new ArrayList<>();
        if (files == null || files.length == 0) {
            return results;
        }
        Path uploadDir = Path.of("src/main/resources/static/images");
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String fileName = "review-" + UUID.randomUUID() + ext;
            try {
                Files.createDirectories(uploadDir);
                Files.write(uploadDir.resolve(fileName), file.getBytes());
                results.add(fileName);
            } catch (IOException ignored) {
                // ignore failed image save
            }
        }
        return results;
    }
}
