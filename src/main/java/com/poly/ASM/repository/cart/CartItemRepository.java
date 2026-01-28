package com.poly.ASM.repository.cart;

import com.poly.ASM.entity.cart.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    @Query("""
            select ci
            from CartItemEntity ci
            join fetch ci.product p
            join fetch ci.size s
            where ci.account.username = :username
            order by ci.createdAt desc, ci.id desc
            """)
    List<CartItemEntity> findByUsernameWithRefs(@Param("username") String username);

    Optional<CartItemEntity> findByAccountUsernameAndProductIdAndSizeId(String username, Integer productId, Integer sizeId);

    @Query("""
            select count(distinct ci.product.id)
            from CartItemEntity ci
            where ci.account.username = :username
            """)
    long countDistinctProductsByUsername(@Param("username") String username);

    @Query("""
            select distinct ci.product.id
            from CartItemEntity ci
            where ci.account.username = :username
            """)
    List<Integer> findDistinctProductIdsByUsername(@Param("username") String username);

    void deleteByAccountUsername(String username);
}

