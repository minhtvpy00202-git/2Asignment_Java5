package com.poly.ASM.controller.web;

import com.poly.ASM.service.cart.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final HttpSession session;

    @GetMapping("/cart/index")
    public String index(Model model) {
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        Object message = session.getAttribute("CART_MESSAGE");
        if (message instanceof String msg && !msg.isBlank()) {
            model.addAttribute("message", msg);
            session.removeAttribute("CART_MESSAGE");
        }
        return "cart/index";
    }

    @GetMapping("/cart/add/{id}")
    public String add(@PathVariable("id") Integer productId,
                      @RequestParam(value = "sizeId", required = false) Integer sizeId) {
        if (sizeId == null) {
            session.setAttribute("CART_MESSAGE", "Vui lòng chọn size trước khi thêm vào giỏ hàng.");
            return "redirect:/cart/index";
        }
        boolean ok = cartService.add(productId, sizeId, 1);
        if (!ok) {
            session.setAttribute("CART_MESSAGE", "Size đã chọn đã hết hàng hoặc không đủ tồn kho.");
        }
        return "redirect:/cart/index";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addDetail(@RequestParam("productId") Integer productId,
                                                         @RequestParam("sizeId") Integer sizeId,
                                                         @RequestParam(value = "quantity", defaultValue = "1") Integer quantity) {
        if (sizeId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn size trước khi thêm vào giỏ hàng."));
        }
        boolean ok = cartService.addToCart(productId, sizeId, quantity);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số lượng vượt quá tồn kho của size đã chọn."));
        }
        long distinctCount = cartService.getDistinctProductCount();
        return ResponseEntity.ok(Map.of(
                "message", "Đã thêm vào giỏ hàng.",
                "distinctCount", distinctCount,
                "productId", productId
        ));
    }

    @GetMapping("/cart/remove/{id}")
    public String remove(@PathVariable("id") Integer productId,
                         @RequestParam("sizeId") Integer sizeId) {
        cartService.remove(productId, sizeId);
        return "redirect:/cart/index";
    }

    @PostMapping("/cart/update")
    public String update(@RequestParam("productId") Integer productId,
                         @RequestParam("sizeId") Integer sizeId,
                         @RequestParam("quantity") Integer quantity) {
        boolean ok = cartService.update(productId, sizeId, quantity);
        if (!ok) {
            session.setAttribute("CART_MESSAGE", "Số lượng vượt quá tồn kho của size đã chọn.");
        }
        return "redirect:/cart/index";
    }

    @GetMapping("/cart/clear")
    public String clear() {
        cartService.clear();
        return "redirect:/cart/index";
    }
}
