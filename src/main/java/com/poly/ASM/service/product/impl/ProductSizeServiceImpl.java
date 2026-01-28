package com.poly.ASM.service.product.impl;

import com.poly.ASM.entity.product.ProductSize;
import com.poly.ASM.repository.product.ProductSizeRepository;
import com.poly.ASM.service.product.ProductSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductSizeServiceImpl implements ProductSizeService {

    private final ProductSizeRepository productSizeRepository;

    @Override
    public List<ProductSize> findByProductId(Integer productId) {
        return productSizeRepository.findByProductId(productId);
    }

    @Override
    public List<ProductSize> findByProductIds(List<Integer> productIds) {
        return productSizeRepository.findByProductIdIn(productIds);
    }

    @Override
    public Optional<ProductSize> findByProductIdAndSizeId(Integer productId, Integer sizeId) {
        return productSizeRepository.findByProductIdAndSizeId(productId, sizeId);
    }

    @Override
    public ProductSize save(ProductSize productSize) {
        return productSizeRepository.save(productSize);
    }

    @Override
    @Transactional
    public void deleteByProductId(Integer productId) {
        productSizeRepository.deleteByProductId(productId);
    }
}
