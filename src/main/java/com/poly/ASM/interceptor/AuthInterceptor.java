package com.poly.ASM.interceptor;

import com.poly.ASM.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String REDIRECT_AFTER_LOGIN = "REDIRECT_AFTER_LOGIN";
    private static final List<String> PROTECTED_PREFIXES = List.of(
            "/order/", "/account/", "/admin/"
    );

    private final AuthService authService;
    private final HttpSession session;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();
        boolean protectedPath = PROTECTED_PREFIXES.stream().anyMatch(uri::startsWith);
        if (!protectedPath) {
            return true;
        }

        if (!authService.isAuthenticated()) {
            String query = request.getQueryString();
            String fullUri = query == null || query.isBlank() ? uri : (uri + "?" + query);
            session.setAttribute(REDIRECT_AFTER_LOGIN, fullUri);
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        if (uri.startsWith("/admin/") && !authService.hasRole("ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/home/index");
            return false;
        }

        return true;
    }
}
