package com.vm325.inventory_back.reports;

public interface InventoryReportService {
    byte[] generate(ReportFormat format);
}
