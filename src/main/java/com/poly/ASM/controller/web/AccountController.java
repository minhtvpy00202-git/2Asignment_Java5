package com.poly.ASM.controller.web;

import com.poly.ASM.entity.user.Account;
import com.poly.ASM.entity.user.Authority;
import com.poly.ASM.entity.user.Role;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.user.AccountService;
import com.poly.ASM.service.user.AuthorityService;
import com.poly.ASM.service.user.RoleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private static final String SESSION_USER_KEY = "USER";

    private final AccountService accountService;
    private final RoleService roleService;
    private final AuthorityService authorityService;
    private final AuthService authService;
    private final HttpSession session;

    @GetMapping("/account/sign-up")
    public String signUpForm() {
        return "account/sign-up";
    }

    @PostMapping("/account/sign-up")
    public String signUp(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         @RequestParam("fullname") String fullname,
                         @RequestParam("email") String email,
                         Model model) {
        if (accountService.findByUsername(username).isPresent()) {
            model.addAttribute("message", "Username đã tồn tại");
            return "account/sign-up";
        }
        if (accountService.findByEmail(email).isPresent()) {
            model.addAttribute("message", "Email đã tồn tại");
            return "account/sign-up";
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        account.setFullname(fullname);
        account.setEmail(email);
        account.setActivated(true);
        Account saved = accountService.create(account);

        Role role = roleService.findById("USER")
                .orElseGet(() -> roleService.create(new Role("USER", "Khach hang", null)));
        Authority authority = new Authority();
        authority.setAccount(saved);
        authority.setRole(role);
        authorityService.create(authority);

        model.addAttribute("message", "Đăng ký thành công, vui lòng đăng nhập");
        return "account/sign-up";
    }

    @GetMapping("/account/edit-profile")
    public String editProfileForm(Model model) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("account", user);
        return "account/edit-profile";
    }

    @PostMapping("/account/edit-profile")
    public String editProfile(@RequestParam("fullname") String fullname,
                              @RequestParam("email") String email,
                              @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                              Model model) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        Optional<Account> byEmail = accountService.findByEmail(email);
        if (byEmail.isPresent() && !byEmail.get().getUsername().equals(user.getUsername())) {
            model.addAttribute("message", "Email đã tồn tại");
            model.addAttribute("account", user);
            return "account/edit-profile";
        }

        user.setFullname(fullname);
        user.setEmail(email);
        String photoName = savePhoto(photoFile);
        if (photoName != null) {
            user.setPhoto(photoName);
        }
        Account saved = accountService.update(user);
        session.setAttribute(SESSION_USER_KEY, saved);
        model.addAttribute("account", saved);
        model.addAttribute("message", "Cập nhật thành công");
        return "account/edit-profile";
    }

    private String savePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String fileName = "avatar-" + UUID.randomUUID() + ext;
        Path uploadDir = Path.of("src/main/resources/static/images");
        try {
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(fileName), file.getBytes());
            return fileName;
        } catch (IOException e) {
            return null;
        }
    }

    @GetMapping("/account/change-password")
    public String changePasswordForm() {
        return "account/change-password";
    }

    @PostMapping("/account/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Model model) {
        Account user = authService.getUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        if (!currentPassword.equals(user.getPassword())) {
            model.addAttribute("message", "Mật khẩu hiện tại không đúng");
            return "account/change-password";
        }
        user.setPassword(newPassword);
        Account saved = accountService.update(user);
        session.setAttribute(SESSION_USER_KEY, saved);
        model.addAttribute("message", "Đổi mật khẩu thành công");
        return "account/change-password";
    }

    @GetMapping("/account/forgot-password")
    public String forgotPasswordForm() {
        return "account/forgot-password";
    }

    @PostMapping("/account/forgot-password")
    public String forgotPassword(@RequestParam("email") String email, Model model) {
        Optional<Account> account = accountService.findByEmail(email);
        if (account.isEmpty()) {
            model.addAttribute("message", "Email không tồn tại");
            return "account/forgot-password";
        }
        Account user = account.get();
        user.setPassword("123456");
        accountService.update(user);
        model.addAttribute("message", "Mật khẩu mới là 123456");
        return "account/forgot-password";
    }
}
