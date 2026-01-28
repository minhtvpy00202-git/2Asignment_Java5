package com.poly.ASM.service.user;

import com.poly.ASM.entity.user.Authority;

import java.util.List;
import java.util.Optional;

public interface AuthorityService {

    List<Authority> findAll();

    Optional<Authority> findById(Long id);

    List<Authority> findByAccountUsername(String username);

    Authority create(Authority authority);

    Authority update(Authority authority);

    void deleteById(Long id);

    void deleteByAccountUsername(String username);
}
