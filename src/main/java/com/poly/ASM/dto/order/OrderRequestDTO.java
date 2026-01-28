package com.poly.ASM.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    private String username;
    private String address;
    private String status;
    private List<OrderItemRequestDTO> items = new ArrayList<>();
}
