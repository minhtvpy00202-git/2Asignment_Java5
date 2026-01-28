package com.poly.ASM.service.user.impl;

import com.poly.ASM.entity.user.Authority;
import com.poly.ASM.repository.user.AuthorityRepository;
import com.poly.ASM.service.user.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Override
    public List<Authority> findAll() {
        return authorityRepository.findAll();
    }

    @Override
    public Optional<Authority> findById(Long id) {
        return authorityRepository.findById(id);
    }

    @Override
    public List<Authority> findByAccountUsername(String username) {
        return authorityRepository.findByAccountUsername(username);
    }

    @Override
    public Authority create(Authority authority) {
        return authorityRepository.save(authority);
    }

    @Override
    public Authority update(Authority authority) {
        return authorityRepository.save(authority);
    }

    @Override
    public void deleteById(Long id) {
        authorityRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByAccountUsername(String username) {
        authorityRepository.deleteByAccountUsername(username);
    }
}
