package com.vm325.inventory_back.reports;

import java.time.LocalDateTime;

public interface SalesReportService {
    byte[] generate(LocalDateTime from, LocalDateTime to, ReportFormat format);
}
