package com.poly.ASM.service.product.impl;

import com.poly.ASM.entity.product.Size;
import com.poly.ASM.repository.product.SizeRepository;
import com.poly.ASM.service.product.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;

    @Override
    public List<Size> findAll() {
        return sizeRepository.findAll();
    }

    @Override
    public Optional<Size> findById(Integer id) {
        return sizeRepository.findById(id);
    }
}
