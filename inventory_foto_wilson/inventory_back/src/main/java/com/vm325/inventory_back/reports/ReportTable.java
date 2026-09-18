package com.vm325.inventory_back.reports;

import java.util.List;

/**
 * Modelo agnóstico de formato para una tabla de reporte: un título, sus
 * encabezados, filas de datos ya formateadas como texto, y líneas de pie
 * (totales/resúmenes). Tanto {@link PdfReportWriter} como
 * {@link XlsxReportWriter} consumen exactamente esta misma estructura, así
 * que agregar un reporte nuevo no requiere tocar ninguno de los dos.
 */
public record ReportTable(
        String title,
        List<String> headers,
        List<List<String>> rows,
        List<String> footerLines
) {
}
