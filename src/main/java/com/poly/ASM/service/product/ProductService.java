package com.poly.ASM.service.product;

import com.poly.ASM.entity.product.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductService {

    List<Product> findAll();

    Page<Product> findAllPage(int page, int size);

    Optional<Product> findById(Integer id);

    Optional<Product> findByIdWithSizes(Integer id);

    List<Product> findTop8ByOrderByCreateDateDesc();

    List<Product> findTop8ByDiscountGreaterThanOrderByDiscountDesc(double discount);

    List<Product> findTop8BestSeller();

    List<Product> findByCategoryId(String categoryId);

    List<Product> findTop4ByCategoryIdAndIdNot(String categoryId, Integer id);

    List<Product> findAllWithSizes();

    List<Product> searchWithFilters(String keyword,
                                    String categoryId,
                                    BigDecimal minPrice,
                                    BigDecimal maxPrice,
                                    String sort);

    Page<Product> searchWithFiltersPage(String keyword,
                                        String categoryId,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        String sort,
                                        int page,
                                        int size);

    Product create(Product product);

    Product update(Product product);

    void deleteById(Integer id);
}
