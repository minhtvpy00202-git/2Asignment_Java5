package com.poly.ASM.repository.product;

import com.poly.ASM.entity.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
}
