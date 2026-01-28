package com.poly.ASM.service.review;

import com.poly.ASM.entity.order.Order;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.review.ProductReview;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.repository.review.ProductReviewStats;

import java.util.List;
import java.util.Set;

public interface ProductReviewService {

    boolean hasReviewed(String username, Integer productId, Long orderId);

    Set<Integer> findReviewedProductIds(String username, Long orderId);

    ProductReview createReview(Account account,
                               Product product,
                               Order order,
                               Integer starRating,
                               String reviewContent,
                               List<String> images);

    List<ProductReview> findByProductId(Integer productId);

    ProductReviewStats getStats(Integer productId);
}
