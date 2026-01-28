package com.poly.ASM.controller.admin;

import com.poly.ASM.entity.user.Account;
import com.poly.ASM.entity.user.Authority;
import com.poly.ASM.entity.user.Role;
import com.poly.ASM.service.user.AccountService;
import com.poly.ASM.service.user.AuthorityService;
import com.poly.ASM.service.user.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountAController {

    private final AccountService accountService;
    private final RoleService roleService;
    private final AuthorityService authorityService;

    @GetMapping("/admin/account/index")
    public String index(Model model) {
        model.addAttribute("accounts", accountService.findAll());
        model.addAttribute("roles", roleService.findAll());
        model.addAttribute("account", new Account());
        model.addAttribute("roleId", "USER");
        return "admin/account";
    }

    @PostMapping("/admin/account/create")
    public String create(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         @RequestParam("fullname") String fullname,
                         @RequestParam("email") String email,
                         @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                         @RequestParam(value = "activated", required = false) Boolean activated,
                         @RequestParam("roleId") String roleId) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        account.setFullname(fullname);
        account.setEmail(email);
        String photoName = saveImage(photoFile);
        if (photoName != null) {
            account.setPhoto(photoName);
        }
        account.setActivated(activated != null ? activated : true);
        Account saved = accountService.create(account);

        Role role = roleService.findById(roleId)
                .orElseGet(() -> roleService.create(new Role(roleId, roleId, null)));
        Authority authority = new Authority();
        authority.setAccount(saved);
        authority.setRole(role);
        authorityService.create(authority);

        return "redirect:/admin/account/index";
    }

    @GetMapping("/admin/account/edit/{username}")
    public String edit(@PathVariable("username") String username, Model model) {
        Optional<Account> account = accountService.findByUsername(username);
        model.addAttribute("accounts", accountService.findAll());
        model.addAttribute("roles", roleService.findAll());
        model.addAttribute("account", account.orElseGet(Account::new));

        String roleId = "USER";
        List<Authority> authorities = authorityService.findByAccountUsername(username);
        if (!authorities.isEmpty() && authorities.get(0).getRole() != null) {
            roleId = authorities.get(0).getRole().getId();
        }
        model.addAttribute("roleId", roleId);
        return "admin/account";
    }

    @PostMapping("/admin/account/update")
    public String update(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         @RequestParam("fullname") String fullname,
                         @RequestParam("email") String email,
                         @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                         @RequestParam(value = "activated", required = false) Boolean activated,
                         @RequestParam("roleId") String roleId) {
        Account account = accountService.findByUsername(username).orElseGet(Account::new);
        account.setUsername(username);
        account.setPassword(password);
        account.setFullname(fullname);
        account.setEmail(email);
        String photoName = saveImage(photoFile);
        if (photoName != null) {
            account.setPhoto(photoName);
        }
        account.setActivated(activated != null ? activated : true);
        Account saved = accountService.update(account);

        authorityService.deleteByAccountUsername(username);
        Role role = roleService.findById(roleId)
                .orElseGet(() -> roleService.create(new Role(roleId, roleId, null)));
        Authority authority = new Authority();
        authority.setAccount(saved);
        authority.setRole(role);
        authorityService.create(authority);

        return "redirect:/admin/account/index";
    }

    @GetMapping("/admin/account/delete/{username}")
    public String delete(@PathVariable("username") String username) {
        authorityService.deleteByAccountUsername(username);
        accountService.deleteByUsername(username);
        return "redirect:/admin/account/index";
    }

    private String saveImage(MultipartFile file) {
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
}
