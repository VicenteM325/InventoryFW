package com.vm325.inventory_back.reports;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Exportador a .xlsx genérico: una hoja por {@link ReportTable}, con
 * encabezado en negrita, filas de datos y líneas de pie en cursiva al
 * final. No sabe nada de ventas ni de inventario.
 */
@Component
public class XlsxReportWriter implements ReportWriter {

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.XLSX;
    }

    @Override
    public byte[] write(List<ReportTable> tables) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle footerStyle = footerStyle(workbook);

            for (ReportTable table : tables) {
                XSSFSheet sheet = workbook.createSheet(safeSheetName(table.title()));
                int rowIndex = 0;

                Row headerRow = sheet.createRow(rowIndex++);
                for (int col = 0; col < table.headers().size(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(table.headers().get(col));
                    cell.setCellStyle(headerStyle);
                }

                for (List<String> dataRow : table.rows()) {
                    Row row = sheet.createRow(rowIndex++);
                    for (int col = 0; col < dataRow.size(); col++) {
                        row.createCell(col).setCellValue(dataRow.get(col));
                    }
                }

                if (!table.footerLines().isEmpty()) {
                    rowIndex++; // fila en blanco antes del pie
                    for (String line : table.footerLines()) {
                        Row footerRow = sheet.createRow(rowIndex++);
                        Cell cell = footerRow.createCell(0);
                        cell.setCellValue(line);
                        cell.setCellStyle(footerStyle);
                    }
                }

                for (int col = 0; col < table.headers().size(); col++) {
                    sheet.autoSizeColumn(col);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error generando el archivo Excel", e);
        }
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(boldFont);
        return style;
    }

    private CellStyle footerStyle(XSSFWorkbook workbook) {
        Font italicFont = workbook.createFont();
        italicFont.setItalic(true);
        italicFont.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(italicFont);
        return style;
    }

    private String safeSheetName(String title) {
        String sanitized = title.replaceAll("[\\[\\]:*?/\\\\]", " ").trim();
        return sanitized.length() > 31 ? sanitized.substring(0, 31) : sanitized;
    }
}
