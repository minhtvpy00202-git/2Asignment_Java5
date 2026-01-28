package com.poly.ASM.service.order;

import com.poly.ASM.repository.order.RevenueReport;
import com.poly.ASM.repository.order.VipReport;
import com.poly.ASM.service.order.dto.RevenueOrderRow;

import java.util.List;
import java.time.LocalDate;

public interface ReportService {

    List<RevenueReport> revenueByCategory();

    List<RevenueOrderRow> revenueByDeliveredOrders(LocalDate fromDate, LocalDate toDate, String sortField, String sortDir);

    List<VipReport> top10VipCustomers();
}
