package com.poly.ASM.controller.web;

import com.poly.ASM.dto.product.ProductDTO;
import com.poly.ASM.dto.product.ProductRequestDTO;
import com.poly.ASM.entity.product.Category;
import com.poly.ASM.entity.product.Product;
import com.poly.ASM.service.product.CategoryService;
import com.poly.ASM.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public List<ProductDTO> findAll() {
        return productService.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> findById(@PathVariable Integer id) {
        return productService.findById(id)
                .map(product -> ResponseEntity.ok(toDto(product)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequestDTO request) {
        Optional<Category> category = resolveCategory(request.getCategoryId());
        if (request.getCategoryId() != null && category.isEmpty()) {
            return ResponseEntity.badRequest().body("Category not found");
        }

        Product product = new Product();
        applyRequest(product, request);
        category.ifPresent(product::setCategory);
        Product saved = productService.create(product);
        return ResponseEntity.ok(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody ProductRequestDTO request) {
        Optional<Product> existing = productService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Category> category = resolveCategory(request.getCategoryId());
        if (request.getCategoryId() != null && category.isEmpty()) {
            return ResponseEntity.badRequest().body("Category not found");
        }

        Product product = existing.get();
        applyRequest(product, request);
        if (request.getCategoryId() != null) {
            product.setCategory(category.orElse(null));
        }

        Product saved = productService.update(product);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ProductDTO toDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImage(product.getImage());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setAvailable(product.getAvailable());
        dto.setQuantity(product.getQuantity());
        dto.setDescription(product.getDescription());
        dto.setCreateDate(product.getCreateDate());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }

    private void applyRequest(Product product, ProductRequestDTO request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getImage() != null) {
            product.setImage(request.getImage());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDiscount() != null) {
            product.setDiscount(request.getDiscount());
        }
        if (request.getAvailable() != null) {
            product.setAvailable(request.getAvailable());
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
    }

    private Optional<Category> resolveCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return Optional.empty();
        }
        return categoryService.findById(categoryId);
    }
}
