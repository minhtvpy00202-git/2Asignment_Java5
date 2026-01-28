package com.poly.ASM.service.user.impl;

import com.poly.ASM.entity.user.Role;
import com.poly.ASM.repository.user.RoleRepository;
import com.poly.ASM.service.user.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findById(String id) {
        return roleRepository.findById(id);
    }

    @Override
    public Role create(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role update(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public void deleteById(String id) {
        roleRepository.deleteById(id);
    }
}
