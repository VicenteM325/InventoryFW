package com.vm325.inventory_back.controllers;

import com.vm325.inventory_back.reports.InventoryReportService;
import com.vm325.inventory_back.reports.ReportFormat;
import com.vm325.inventory_back.reports.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Reportes de ventas e inventario, exportables en Excel y PDF (RF-07).
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
public class ReportController {

    private static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SalesReportService salesReportService;
    private final InventoryReportService inventoryReportService;

    @GetMapping("/sales")
    public ResponseEntity<byte[]> salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "PDF") String format) {

        LocalDate today = LocalDate.now();
        LocalDate effectiveFrom = (from != null) ? from : today;
        LocalDate effectiveTo = (to != null) ? to : today;

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha 'from' no puede ser posterior a 'to'");
        }

        LocalDateTime fromDateTime = effectiveFrom.atStartOfDay();
        LocalDateTime toDateTime = effectiveTo.plusDays(1).atStartOfDay().minusNanos(1);

        ReportFormat reportFormat = parseFormat(format);
        byte[] report = salesReportService.generate(fromDateTime, toDateTime, reportFormat);

        String filename = "reporte_ventas_" + effectiveFrom.format(FILENAME_DATE)
                + "_" + effectiveTo.format(FILENAME_DATE) + "." + reportFormat.getFileExtension();

        return respond(report, reportFormat, filename);
    }

    @GetMapping("/inventory")
    public ResponseEntity<byte[]> inventoryReport(@RequestParam(defaultValue = "PDF") String format) {
        ReportFormat reportFormat = parseFormat(format);
        byte[] report = inventoryReportService.generate(reportFormat);

        String filename = "reporte_inventario_" + LocalDate.now().format(FILENAME_DATE)
                + "." + reportFormat.getFileExtension();

        return respond(report, reportFormat, filename);
    }

    private ResponseEntity<byte[]> respond(byte[] report, ReportFormat format, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(format.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(report);
    }

    private ReportFormat parseFormat(String format) {
        try {
            return ReportFormat.valueOf(format.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato no soportado: " + format + " (use PDF o XLSX)");
        }
    }
}
