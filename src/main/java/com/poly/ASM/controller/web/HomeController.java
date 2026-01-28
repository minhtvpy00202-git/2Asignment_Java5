package com.poly.ASM.controller.web;

import com.poly.ASM.entity.product.Category;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.service.product.CategoryService;
import com.poly.ASM.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/home/index")
    public String index(@RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "categoryId", required = false) String categoryId,
                        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                        @RequestParam(value = "priceRange", required = false) String priceRange,
                        @RequestParam(value = "sort", required = false) String sort,
                        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                        @RequestParam(value = "size", required = false, defaultValue = "12") Integer size,
                        Model model) {
        BigDecimal[] range = applyQuickRange(priceRange, minPrice, maxPrice);
        minPrice = range[0];
        maxPrice = range[1];
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("sort", sort);
        model.addAttribute("pageSize", size);

        boolean hasFilter = (keyword != null && !keyword.isBlank())
                || (categoryId != null && !categoryId.isBlank())
                || minPrice != null
                || maxPrice != null
                || (priceRange != null && !priceRange.isBlank())
                || (sort != null && !sort.isBlank());
        if (hasFilter) {
            Page<Product> filteredPage = productService.searchWithFiltersPage(keyword, categoryId, minPrice, maxPrice, sort, page, size);
            model.addAttribute("filteredProducts", filteredPage.getContent());
            model.addAttribute("filteredPage", filteredPage);
            model.addAttribute("currentPage", filteredPage.getNumber());
            model.addAttribute("totalPages", filteredPage.getTotalPages());
        }
        model.addAttribute("newProducts", productService.findTop8ByOrderByCreateDateDesc());
        model.addAttribute("discountProducts", productService.findTop8ByDiscountGreaterThanOrderByDiscountDesc(0));
        model.addAttribute("bestSellerProducts", productService.findTop8BestSeller());
        return "home/index";
    }

    private BigDecimal[] applyQuickRange(String priceRange, BigDecimal minPrice, BigDecimal maxPrice) {
        if (priceRange == null || priceRange.isBlank()) {
            return new BigDecimal[]{minPrice, maxPrice};
        }
        return switch (priceRange) {
            case "under_500" -> new BigDecimal[]{null, BigDecimal.valueOf(500_000)};
            case "500_1m" -> new BigDecimal[]{BigDecimal.valueOf(500_000), BigDecimal.valueOf(1_000_000)};
            case "1m_2m" -> new BigDecimal[]{BigDecimal.valueOf(1_000_000), BigDecimal.valueOf(2_000_000)};
            case "over_2m" -> new BigDecimal[]{BigDecimal.valueOf(2_000_000), null};
            default -> new BigDecimal[]{minPrice, maxPrice};
        };
    }
}
