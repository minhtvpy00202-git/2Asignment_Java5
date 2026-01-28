package com.poly.ASM.service.user;

import com.poly.ASM.entity.user.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    List<Role> findAll();

    Optional<Role> findById(String id);

    Role create(Role role);

    Role update(Role role);

    void deleteById(String id);
}
