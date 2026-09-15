package com.vm325.inventory_back.reports;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Exportador a PDF genérico: dibuja cada {@link ReportTable} como una tabla
 * simple (columnas de ancho uniforme, texto truncado si no cabe) en hojas
 * tamaño carta, paginando automáticamente y repitiendo el encabezado de
 * columnas en cada página nueva de una misma tabla. No sabe nada de ventas
 * ni de inventario.
 */
@Component
public class PdfReportWriter implements ReportWriter {

    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
    private static final float MARGIN = 40f;
    private static final float ROW_HEIGHT = 16f;
    private static final float TITLE_FONT_SIZE = 14f;
    private static final float HEADER_FONT_SIZE = 9f;
    private static final float DATA_FONT_SIZE = 9f;
    private static final float FOOTER_FONT_SIZE = 10f;

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.PDF;
    }

    @Override
    public byte[] write(List<ReportTable> tables) {
        try (PDDocument document = new PDDocument()) {
            PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            for (ReportTable table : tables) {
                renderTable(document, table, regularFont, boldFont);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error generando el reporte PDF", e);
        }
    }

    private void renderTable(PDDocument document, ReportTable table, PDFont regularFont, PDFont boldFont) throws IOException {
        float contentWidth = PAGE_WIDTH - 2 * MARGIN;
        int columnCount = Math.max(1, table.headers().size());
        float columnWidth = contentWidth / columnCount;

        PageCursor cursor = new PageCursor(document);
        cursor.newPage();

        // Título de la tabla
        cursor.drawLine(boldFont, TITLE_FONT_SIZE, MARGIN, table.title());
        cursor.advance(TITLE_FONT_SIZE + 10);

        drawHeaderRow(cursor, table, boldFont, columnWidth);

        for (List<String> row : table.rows()) {
            if (cursor.remainingHeight() < ROW_HEIGHT) {
                cursor.closePage();
                cursor.newPage();
                drawHeaderRow(cursor, table, boldFont, columnWidth);
            }
            drawDataRow(cursor, row, regularFont, columnWidth);
        }

        if (!table.footerLines().isEmpty()) {
            cursor.advance(ROW_HEIGHT / 2);
            for (String line : table.footerLines()) {
                if (cursor.remainingHeight() < ROW_HEIGHT) {
                    cursor.closePage();
                    cursor.newPage();
                }
                cursor.drawLine(boldFont, FOOTER_FONT_SIZE, MARGIN, line);
                cursor.advance(ROW_HEIGHT);
            }
        }

        cursor.closePage();
    }

    private void drawHeaderRow(PageCursor cursor, ReportTable table, PDFont boldFont, float columnWidth) throws IOException {
        for (int col = 0; col < table.headers().size(); col++) {
            float x = MARGIN + col * columnWidth;
            cursor.drawCell(boldFont, HEADER_FONT_SIZE, x, columnWidth, table.headers().get(col));
        }
        // La regla se dibuja ANTES de avanzar a la fila siguiente: debe
        // quedar entre el encabezado y la primera fila de datos, no sobre
        // el texto de esta última.
        cursor.drawHorizontalRule(MARGIN, PAGE_WIDTH - MARGIN);
        cursor.advance(ROW_HEIGHT);
    }

    private void drawDataRow(PageCursor cursor, List<String> row, PDFont regularFont, float columnWidth) throws IOException {
        for (int col = 0; col < row.size(); col++) {
            float x = MARGIN + col * columnWidth;
            cursor.drawCell(regularFont, DATA_FONT_SIZE, x, columnWidth, row.get(col));
        }
        cursor.advance(ROW_HEIGHT);
    }

    /**
     * Mantiene el estado (página actual, content stream, posición Y) al
     * dibujar una tabla que puede abarcar varias páginas: los content
     * streams de PDFBox no pueden extenderse entre páginas, así que hay que
     * cerrarlos y abrir uno nuevo por cada página.
     */
    private static final class PageCursor {
        private final PDDocument document;
        private PDPageContentStream stream;
        private float y;

        PageCursor(PDDocument document) {
            this.document = document;
        }

        void newPage() throws IOException {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        void closePage() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }

        float remainingHeight() {
            return y - MARGIN;
        }

        void advance(float amount) {
            y -= amount;
        }

        void drawLine(PDFont font, float fontSize, float x, String text) throws IOException {
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(x, y);
            stream.showText(sanitize(text));
            stream.endText();
        }

        void drawCell(PDFont font, float fontSize, float x, float columnWidth, String text) throws IOException {
            String truncated = truncateToWidth(font, fontSize, text, columnWidth - 4);
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(x, y);
            stream.showText(sanitize(truncated));
            stream.endText();
        }

        void drawHorizontalRule(float x1, float x2) throws IOException {
            // y es la línea base del texto ya dibujado en esta posición;
            // -4 la baja lo suficiente para quedar bajo los descendentes
            // del encabezado sin invadir los ascendentes de la fila
            // siguiente (una fila más abajo, a y - ROW_HEIGHT).
            float lineY = y - 4;
            stream.setLineWidth(0.5f);
            stream.moveTo(x1, lineY);
            stream.lineTo(x2, lineY);
            stream.stroke();
        }

        private String truncateToWidth(PDFont font, float fontSize, String text, float maxWidth) throws IOException {
            String safe = sanitize(text);
            if (font.getStringWidth(safe) / 1000f * fontSize <= maxWidth) {
                return safe;
            }
            String ellipsis = "...";
            StringBuilder sb = new StringBuilder();
            for (char c : safe.toCharArray()) {
                String candidate = sb.toString() + c + ellipsis;
                if (font.getStringWidth(candidate) / 1000f * fontSize > maxWidth) {
                    break;
                }
                sb.append(c);
            }
            return sb + ellipsis;
        }

        // PDFBox's standard 14 fonts (WinAnsiEncoding) can't encode every
        // Unicode character; replace anything outside the printable Latin-1
        // range so showText never throws on stray characters.
        private String sanitize(String text) {
            if (text == null) return "";
            StringBuilder sb = new StringBuilder(text.length());
            for (char c : text.toCharArray()) {
                sb.append(c <= 0xFF ? c : '?');
            }
            return sb.toString();
        }
    }
}
