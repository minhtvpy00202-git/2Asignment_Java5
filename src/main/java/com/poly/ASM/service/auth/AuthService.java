package com.poly.ASM.service.auth;

import com.poly.ASM.entity.user.Account;

public interface AuthService {

    boolean login(String username, String password);

    void logout();

    Account getUser();

    boolean isAuthenticated();

    boolean hasRole(String role);
}
