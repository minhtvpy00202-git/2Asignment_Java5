package com.poly.ASM.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String name;
    private String image;
    private BigDecimal price;
    private BigDecimal discount;
    private Boolean available;
    private Integer quantity;
    private String description;
    private String categoryId;
}
