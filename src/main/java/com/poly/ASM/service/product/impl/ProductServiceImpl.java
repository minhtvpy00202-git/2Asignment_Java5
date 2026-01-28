package com.poly.ASM.service.product.impl;

import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.product.ProductSize;
import com.poly.ASM.repository.product.ProductRepository;
import com.poly.ASM.service.product.ProductService;
import com.poly.ASM.service.product.ProductSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSizeService productSizeService;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> findAllPage(int page, int size) {
        Page<Product> products = productRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        attachSizes(products.getContent());
        return products;
    }

    @Override
    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> findByIdWithSizes(Integer id) {
        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(this::attachSizes);
        return product;
    }

    @Override
    public List<Product> findTop8ByOrderByCreateDateDesc() {
        List<Product> products = productRepository.findTop8ByOrderByCreateDateDesc();
        attachSizes(products);
        return products;
    }

    @Override
    public List<Product> findTop8ByDiscountGreaterThanOrderByDiscountDesc(double discount) {
        List<Product> products = productRepository.findTop8ByDiscountGreaterThanOrderByDiscountDesc(BigDecimal.valueOf(discount));
        attachSizes(products);
        return products;
    }

    @Override
    public List<Product> findTop8BestSeller() {
        List<Product> products = productRepository.findBestSeller(PageRequest.of(0, 8));
        attachSizes(products);
        return products;
    }

    @Override
    public List<Product> findByCategoryId(String categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        attachSizes(products);
        return products;
    }

    @Override
    public List<Product> findTop4ByCategoryIdAndIdNot(String categoryId, Integer id) {
        List<Product> products = productRepository.findTop4ByCategoryIdAndIdNot(categoryId, id);
        attachSizes(products);
        return products;
    }

    @Override
    public List<Product> findAllWithSizes() {
        List<Product> products = productRepository.findAll();
        attachSizes(products);
        return products;
    }

    @Override
    public List<Product> searchWithFilters(String keyword,
                                           String categoryId,
                                           BigDecimal minPrice,
                                           BigDecimal maxPrice,
                                           String sort) {
        String keywordValue = keyword != null ? keyword.trim() : null;
        String categoryValue = categoryId != null ? categoryId.trim() : null;
        List<Product> products;
        if ("price_asc".equalsIgnoreCase(sort)) {
            products = productRepository.searchOrderByPriceAsc(keywordValue, categoryValue, minPrice, maxPrice);
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            products = productRepository.searchOrderByPriceDesc(keywordValue, categoryValue, minPrice, maxPrice);
        } else {
            products = productRepository.search(keywordValue, categoryValue, minPrice, maxPrice);
        }
        attachSizes(products);
        return products;
    }

    @Override
    public Page<Product> searchWithFiltersPage(String keyword,
                                               String categoryId,
                                               BigDecimal minPrice,
                                               BigDecimal maxPrice,
                                               String sort,
                                               int page,
                                               int size) {
        String keywordValue = keyword != null ? keyword.trim() : null;
        String categoryValue = categoryId != null ? categoryId.trim() : null;
        PageRequest pageRequest;
        if ("price_asc".equalsIgnoreCase(sort)) {
            pageRequest = PageRequest.of(page, size, Sort.by("price").ascending());
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            pageRequest = PageRequest.of(page, size, Sort.by("price").descending());
        } else {
            pageRequest = PageRequest.of(page, size);
        }
        Page<Product> products = productRepository.searchPage(keywordValue, categoryValue, minPrice, maxPrice, pageRequest);
        attachSizes(products.getContent());
        return products;
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }

    private void attachSizes(Product product) {
        List<ProductSize> sizes = productSizeService.findByProductId(product.getId());
        product.setProductSizes(sizes);
    }

    private void attachSizes(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        List<Integer> ids = new ArrayList<>();
        for (Product product : products) {
            ids.add(product.getId());
        }
        List<ProductSize> sizes = productSizeService.findByProductIds(ids);
        Map<Integer, List<ProductSize>> map = new HashMap<>();
        for (ProductSize size : sizes) {
            Integer productId = size.getProduct().getId();
            map.computeIfAbsent(productId, key -> new ArrayList<>()).add(size);
        }
        for (Product product : products) {
            product.setProductSizes(map.getOrDefault(product.getId(), new ArrayList<>()));
        }
    }
}
