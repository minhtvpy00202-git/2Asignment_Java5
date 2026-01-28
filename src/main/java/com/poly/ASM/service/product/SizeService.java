package com.poly.ASM.service.product;

import com.poly.ASM.entity.product.Size;

import java.util.List;
import java.util.Optional;

public interface SizeService {

    List<Size> findAll();

    Optional<Size> findById(Integer id);
}
