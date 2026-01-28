package com.poly.ASM.service.order.impl;

import com.poly.ASM.repository.order.OrderDetailRepository;
import com.poly.ASM.repository.order.OrderRepository;
import com.poly.ASM.repository.order.RevenueReport;
import com.poly.ASM.repository.order.VipReport;
import com.poly.ASM.service.order.ReportService;
import com.poly.ASM.service.order.dto.RevenueOrderRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<RevenueReport> revenueByCategory() {
        return orderDetailRepository.getRevenueByCategory();
    }

    @Override
    public List<RevenueOrderRow> revenueByDeliveredOrders(LocalDate fromDate,
                                                         LocalDate toDate,
                                                         String sortField,
                                                         String sortDir) {
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        List<com.poly.ASM.entity.order.OrderDetail> details =
                orderDetailRepository.findDeliveredDetailsForRevenueInRange(fromDateTime, toDateTime);
        java.util.Map<Long, Integer> counts = new java.util.LinkedHashMap<>();
        for (var d : details) {
            Long orderId = d.getOrder() != null ? d.getOrder().getId() : null;
            if (orderId == null) {
                continue;
            }
            counts.put(orderId, counts.getOrDefault(orderId, 0) + 1);
        }

        java.util.Map<Long, Integer> orderIndexMap = new java.util.LinkedHashMap<>();
        java.util.Map<Long, Integer> seen = new java.util.HashMap<>();
        int index = 1;
        for (Long orderId : counts.keySet()) {
            orderIndexMap.put(orderId, index++);
            seen.put(orderId, 0);
        }

        List<RevenueOrderRow> rows = new java.util.ArrayList<>();
        for (var d : details) {
            Long orderId = d.getOrder() != null ? d.getOrder().getId() : null;
            if (orderId == null) {
                continue;
            }
            int currentSeen = seen.getOrDefault(orderId, 0);
            boolean firstRow = currentSeen == 0;
            seen.put(orderId, currentSeen + 1);

            BigDecimal unitPrice = d.getPrice() != null ? d.getPrice() : BigDecimal.ZERO;
            BigDecimal qty = BigDecimal.valueOf(d.getQuantity() != null ? d.getQuantity() : 0);
            BigDecimal discountPercent = d.getProduct() != null && d.getProduct().getDiscount() != null
                    ? d.getProduct().getDiscount()
                    : BigDecimal.ZERO;
            BigDecimal discountAmount = unitPrice.multiply(qty)
                    .multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(qty).subtract(discountAmount);

            rows.add(new RevenueOrderRow(
                    orderId,
                    d.getProduct() != null ? d.getProduct().getName() : "",
                    d.getQuantity() != null ? d.getQuantity() : 0,
                    unitPrice,
                    discountAmount,
                    lineTotal,
                    firstRow,
                    counts.getOrDefault(orderId, 1),
                    orderIndexMap.getOrDefault(orderId, 0)
            ));
        }
        rows.sort(buildComparator(sortField, sortDir));
        return rows;
    }

    private Comparator<RevenueOrderRow> buildComparator(String sortField, String sortDir) {
        Comparator<RevenueOrderRow> fieldComparator;
        if ("productName".equalsIgnoreCase(sortField)) {
            fieldComparator = Comparator.comparing(RevenueOrderRow::getProductName, String.CASE_INSENSITIVE_ORDER);
        } else if ("quantity".equalsIgnoreCase(sortField)) {
            fieldComparator = Comparator.comparing(RevenueOrderRow::getQuantity, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("unitPrice".equalsIgnoreCase(sortField)) {
            fieldComparator = Comparator.comparing(RevenueOrderRow::getUnitPrice, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("discountAmount".equalsIgnoreCase(sortField)) {
            fieldComparator = Comparator.comparing(RevenueOrderRow::getDiscountAmount, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lineTotal".equalsIgnoreCase(sortField)) {
            fieldComparator = Comparator.comparing(RevenueOrderRow::getLineTotal, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            fieldComparator = Comparator.comparing(RevenueOrderRow::getOrderId, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        boolean desc = "desc".equalsIgnoreCase(sortDir);
        if (desc) {
            fieldComparator = fieldComparator.reversed();
        }

        return fieldComparator.thenComparing(RevenueOrderRow::getOrderId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    @Override
    public List<VipReport> top10VipCustomers() {
        return orderRepository.getVipCustomers(PageRequest.of(0, 10));
    }
}
