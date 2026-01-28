package com.poly.ASM.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Integer id;
    private String name;
    private String image;
    private BigDecimal price;
    private BigDecimal discount;
    private Boolean available;
    private Integer quantity;
    private String description;
    private LocalDateTime createDate;
    private String categoryId;
    private String categoryName;
}
