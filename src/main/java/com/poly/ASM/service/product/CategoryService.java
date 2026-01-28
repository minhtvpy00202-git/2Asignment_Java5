package com.poly.ASM.service.product;

import com.poly.ASM.entity.product.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> findAll();

    Optional<Category> findById(String id);

    Category create(Category category);

    Category update(Category category);

    void deleteById(String id);
}
