package com.poly.ASM.service.product;

import com.poly.ASM.entity.product.ProductSize;

import java.util.List;
import java.util.Optional;

public interface ProductSizeService {

    List<ProductSize> findByProductId(Integer productId);

    List<ProductSize> findByProductIds(List<Integer> productIds);

    Optional<ProductSize> findByProductIdAndSizeId(Integer productId, Integer sizeId);

    ProductSize save(ProductSize productSize);

    void deleteByProductId(Integer productId);
}
