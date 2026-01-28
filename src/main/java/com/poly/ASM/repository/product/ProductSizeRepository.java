package com.poly.ASM.repository.product;

import com.poly.ASM.entity.product.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {

    @Query("select ps from ProductSize ps join fetch ps.size where ps.product.id = ?1")
    List<ProductSize> findByProductId(Integer productId);

    @Query("select ps from ProductSize ps join fetch ps.size where ps.product.id in ?1")
    List<ProductSize> findByProductIdIn(List<Integer> productIds);

    @Query("select ps from ProductSize ps join fetch ps.size where ps.product.id = ?1 and ps.size.id = ?2")
    Optional<ProductSize> findByProductIdAndSizeId(Integer productId, Integer sizeId);

    void deleteByProductId(Integer productId);
}
