package com.poly.ASM.controller.admin;

import com.poly.ASM.entity.product.Product;
import com.poly.ASM.entity.product.ProductSize;
import com.poly.ASM.entity.product.Size;
import com.poly.ASM.service.product.CategoryService;
import com.poly.ASM.service.product.ProductService;
import com.poly.ASM.service.product.ProductSizeService;
import com.poly.ASM.service.product.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProductAController {

    private static final int PAGE_SIZE = 10;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SizeService sizeService;
    private final ProductSizeService productSizeService;

    @GetMapping("/admin/product/index")
    public String index(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<Product> productsPage = productService.findAllPage(page, PAGE_SIZE);
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", productsPage.getNumber());
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("sizes", sizeService.findAll());
        model.addAttribute("product", new Product());
        model.addAttribute("sizeQtyMap", Map.of());
        return "admin/product";
    }

    @PostMapping("/admin/product/create")
    public String create(@RequestParam("name") String name,
                         @RequestParam("price") BigDecimal price,
                         @RequestParam(value = "discount", required = false) BigDecimal discount,
                         @RequestParam(value = "available", required = false) Boolean available,
                         @RequestParam(value = "quantity", required = false) Integer quantity,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam(value = "description", required = false) String description,
                         @RequestParam("categoryId") String categoryId,
                         @RequestParam Map<String, String> params) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDiscount(discount);
        product.setAvailable(available != null ? available : true);
        product.setQuantity(quantity);
        String imageName = saveImage(imageFile);
        if (imageName != null) {
            product.setImage(imageName);
        }
        product.setDescription(description);
        categoryService.findById(categoryId).ifPresent(product::setCategory);
        Product saved = productService.create(product);
        saveProductSizes(saved, params);
        return "redirect:/admin/product/index";
    }

    @GetMapping("/admin/product/edit/{id}")
    public String edit(@PathVariable("id") Integer id,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        Optional<Product> product = productService.findByIdWithSizes(id);
        Page<Product> productsPage = productService.findAllPage(page, PAGE_SIZE);
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", productsPage.getNumber());
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("sizes", sizeService.findAll());
        Product current = product.orElseGet(Product::new);
        model.addAttribute("product", current);
        model.addAttribute("sizeQtyMap", buildSizeQtyMap(current));
        return "admin/product";
    }

    @PostMapping("/admin/product/update")
    public String update(@RequestParam("id") Integer id,
                         @RequestParam("name") String name,
                         @RequestParam("price") BigDecimal price,
                         @RequestParam(value = "discount", required = false) BigDecimal discount,
                         @RequestParam(value = "available", required = false) Boolean available,
                         @RequestParam(value = "quantity", required = false) Integer quantity,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam(value = "description", required = false) String description,
                         @RequestParam("categoryId") String categoryId,
                         @RequestParam Map<String, String> params) {
        Product product = productService.findById(id).orElseGet(Product::new);
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setDiscount(discount);
        product.setAvailable(available != null ? available : true);
        product.setQuantity(quantity);
        String imageName = saveImage(imageFile);
        if (imageName != null) {
            product.setImage(imageName);
        }
        product.setDescription(description);
        categoryService.findById(categoryId).ifPresent(product::setCategory);
        Product saved = productService.update(product);
        productSizeService.deleteByProductId(saved.getId());
        saveProductSizes(saved, params);
        return "redirect:/admin/product/index";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         @RequestParam(value = "page", defaultValue = "0") int page) {
        productService.deleteById(id);
        return "redirect:/admin/product/index?page=" + page;
    }

    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String fileName = "product-" + UUID.randomUUID() + ext;
        Path uploadDir = Path.of("src/main/resources/static/images");
        try {
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(fileName), file.getBytes());
            return fileName;
        } catch (IOException e) {
            return null;
        }
    }

    private void saveProductSizes(Product product, Map<String, String> params) {
        List<Size> sizes = sizeService.findAll();
        for (Size size : sizes) {
            String key = "size_" + size.getId();
            if (!params.containsKey(key)) {
                continue;
            }
            String value = params.get(key);
            int qty = 0;
            try {
                qty = Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                qty = 0;
            }
            if (qty <= 0) {
                continue;
            }
            ProductSize productSize = new ProductSize();
            productSize.setProduct(product);
            productSize.setSize(size);
            productSize.setQuantity(qty);
            productSizeService.save(productSize);
        }
    }

    private Map<Integer, Integer> buildSizeQtyMap(Product product) {
        if (product == null || product.getProductSizes() == null) {
            return Map.of();
        }
        Map<Integer, Integer> map = new java.util.HashMap<>();
        for (ProductSize ps : product.getProductSizes()) {
            if (ps.getSize() != null) {
                map.put(ps.getSize().getId(), ps.getQuantity());
            }
        }
        return map;
    }
}
