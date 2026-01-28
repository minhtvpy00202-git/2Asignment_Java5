package com.poly.ASM.repository.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface VipReport {

    String getFullname();

    BigDecimal getTotalAmount();

    LocalDateTime getFirstOrderDate();

    LocalDateTime getLastOrderDate();
}
