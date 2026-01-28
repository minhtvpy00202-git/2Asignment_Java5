package com.poly.ASM.repository.product;

import com.poly.ASM.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findTop8ByOrderByCreateDateDesc();

    List<Product> findTop8ByDiscountGreaterThanOrderByDiscountDesc(BigDecimal discount);

    List<Product> findByCategoryId(String categoryId);

    List<Product> findTop4ByCategoryIdAndIdNot(String categoryId, Integer id);

    @Query("select p from Product p join p.orderDetails od group by p order by sum(od.quantity) desc")
    List<Product> findBestSeller(Pageable pageable);

    @Query("""
            select p
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            """)
    List<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") String categoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);

    @Query("""
            select p
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            """)
    Page<Product> searchPage(@Param("keyword") String keyword,
                             @Param("categoryId") String categoryId,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             Pageable pageable);

    @Query("""
            select p
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            order by p.price asc
            """)
    List<Product> searchOrderByPriceAsc(@Param("keyword") String keyword,
                                        @Param("categoryId") String categoryId,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice);

    @Query("""
            select p
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            order by p.price desc
            """)
    List<Product> searchOrderByPriceDesc(@Param("keyword") String keyword,
                                         @Param("categoryId") String categoryId,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice);
}
