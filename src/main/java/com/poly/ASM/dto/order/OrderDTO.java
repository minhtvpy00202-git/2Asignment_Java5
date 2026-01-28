package com.poly.ASM.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;
    private String username;
    private String address;
    private String status;
    private LocalDateTime createDate;
    private BigDecimal totalAmount;
    private List<OrderDetailDTO> items = new ArrayList<>();
}
