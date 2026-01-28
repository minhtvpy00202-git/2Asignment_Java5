package com.poly.ASM.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {

    private Long id;
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
