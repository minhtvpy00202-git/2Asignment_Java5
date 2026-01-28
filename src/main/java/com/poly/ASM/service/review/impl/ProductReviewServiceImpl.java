package com.poly.ASM.service.review.impl;

import com.poly.ASM.entity.order.Order;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.review.ProductReview;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.repository.review.ProductReviewRepository;
import com.poly.ASM.repository.review.ProductReviewStats;
import com.poly.ASM.service.review.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;

    @Override
    public boolean hasReviewed(String username, Integer productId, Long orderId) {
        return productReviewRepository.existsByAccountUsernameAndProductIdAndOrderId(username, productId, orderId);
    }

    @Override
    public Set<Integer> findReviewedProductIds(String username, Long orderId) {
        return productReviewRepository.findByAccountUsernameAndOrderId(username, orderId)
                .stream()
                .filter(review -> review.getProduct() != null)
                .map(review -> review.getProduct().getId())
                .collect(Collectors.toSet());
    }

    @Override
    public ProductReview createReview(Account account,
                                      Product product,
                                      Order order,
                                      Integer starRating,
                                      String reviewContent,
                                      List<String> images) {
        String joinedImages = images == null || images.isEmpty()
                ? null
                : images.stream()
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.joining(","));

        ProductReview review = new ProductReview();
        review.setAccount(account);
        review.setProduct(product);
        review.setOrder(order);
        review.setStarRating(starRating);
        review.setReviewContent(reviewContent);
        review.setImages(joinedImages);
        return productReviewRepository.save(review);
    }

    @Override
    public List<ProductReview> findByProductId(Integer productId) {
        return productReviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public ProductReviewStats getStats(Integer productId) {
        return productReviewRepository.getStatsByProductId(productId);
    }
}
