package com.vm325.inventory_back.reports;

import java.util.List;

public interface ReportWriter {
    ReportFormat getFormat();
    byte[] write(List<ReportTable> tables);
}
