package com.poly.ASM.repository.order;

import java.math.BigDecimal;

public interface RevenueReport {

    String getCategoryName();

    BigDecimal getTotalAmount();

    Long getTotalQuantity();

    BigDecimal getMaxPrice();

    BigDecimal getMinPrice();

    Double getAvgPrice();
}
