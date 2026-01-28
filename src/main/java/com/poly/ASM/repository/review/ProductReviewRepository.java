package com.poly.ASM.repository.review;

import com.poly.ASM.entity.review.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    boolean existsByAccountUsernameAndProductIdAndOrderId(String username, Integer productId, Long orderId);

    List<ProductReview> findByAccountUsernameAndOrderId(String username, Long orderId);

    @Query("""
            select r
            from ProductReview r
            join fetch r.account a
            where r.product.id = :productId
            order by r.createdAt desc
            """)
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(@Param("productId") Integer productId);

    @Query("""
            select avg(r.starRating) as avgRating,
                   count(r) as reviewCount
            from ProductReview r
            where r.product.id = :productId
            """)
    ProductReviewStats getStatsByProductId(@Param("productId") Integer productId);
}
