package com.vm325.inventory_back.reports;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportWriterFactory {

    private final Map<ReportFormat, ReportWriter> writersByFormat;

    public ReportWriterFactory(List<ReportWriter> writers) {
        this.writersByFormat = new EnumMap<>(ReportFormat.class);
        for (ReportWriter writer : writers) {
            writersByFormat.put(writer.getFormat(), writer);
        }
    }

    public ReportWriter get(ReportFormat format) {
        ReportWriter writer = writersByFormat.get(format);
        if (writer == null) {
            throw new IllegalArgumentException("Formato de reporte no soportado: " + format);
        }
        return writer;
    }
}
