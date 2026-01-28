package com.poly.ASM.controller.web;

import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.cart.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final String REDIRECT_AFTER_LOGIN = "REDIRECT_AFTER_LOGIN";

    private final AuthService authService;
    private final CartService cartService;
    private final HttpSession session;

    @GetMapping("/auth/login")
    public String loginForm() {
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Model model) {
        boolean ok = authService.login(username, password);
        if (!ok) {
            model.addAttribute("message", "Sai tài khoản hoặc mật khẩu");
            return "auth/login";
        }

        // Merge anonymous cart (session) into user cart (DB) once, right after login.
        cartService.mergeSessionCartToUserCart(username);

        Object redirect = session.getAttribute(REDIRECT_AFTER_LOGIN);
        session.removeAttribute(REDIRECT_AFTER_LOGIN);
        if (redirect instanceof String redirectUrl && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/home/index";
    }

    @GetMapping("/auth/logout")
    public String logout() {
        authService.logout();
        return "redirect:/home/index";
    }
}
