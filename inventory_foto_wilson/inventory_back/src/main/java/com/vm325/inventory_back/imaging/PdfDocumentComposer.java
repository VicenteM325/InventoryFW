package com.vm325.inventory_back.imaging;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Genera el documento en formato PDF, tamaño carta, con las imágenes
 * apiladas verticalmente y una etiqueta opcional sobre cada una (p.ej.
 * "ANVERSO"/"REVERSO").
 */
@Component
public class PdfDocumentComposer implements DocumentComposer {

    private static final float LABEL_HEIGHT_PT = 16f;
    private static final float LABEL_FONT_SIZE = 11f;

    @Override
    public OutputFormat getSupportedFormat() {
        return OutputFormat.PDF;
    }

    @Override
    public byte[] compose(List<BufferedImage> images, DocumentLayoutSpec spec) {
        float pageWidth = (float) spec.pageWidthPt();
        float pageHeight = (float) spec.pageHeightPt();
        float margin = (float) spec.marginPt();
        float contentWidth = pageWidth - 2 * margin;
        float contentHeight = pageHeight - 2 * margin;
        int n = images.size();
        float slotHeight = contentHeight / n;
        boolean hasLabels = !spec.labels().isEmpty();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
            doc.addPage(page);
            PDType1Font labelFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                for (int i = 0; i < n; i++) {
                    BufferedImage image = images.get(i);
                    PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);

                    float slotTop = pageHeight - margin - i * slotHeight;
                    float labelSpace = hasLabels ? LABEL_HEIGHT_PT : 0f;
                    float availableHeight = slotHeight - labelSpace;

                    float imageAspectRatio = (float) image.getWidth() / image.getHeight();
                    float drawWidth = contentWidth;
                    float drawHeight = drawWidth / imageAspectRatio;
                    if (drawHeight > availableHeight) {
                        drawHeight = availableHeight;
                        drawWidth = drawHeight * imageAspectRatio;
                    }

                    float x = margin + (contentWidth - drawWidth) / 2f;
                    float imageAreaTop = slotTop - labelSpace;
                    float y = imageAreaTop - drawHeight - (availableHeight - drawHeight) / 2f;

                    if (hasLabels) {
                        cs.beginText();
                        cs.setFont(labelFont, LABEL_FONT_SIZE);
                        cs.newLineAtOffset(x, slotTop - LABEL_FONT_SIZE);
                        cs.showText(spec.labels().get(i));
                        cs.endText();
                    }

                    cs.drawImage(pdImage, x, y, drawWidth, drawHeight);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error generando el documento PDF", e);
        }
    }
}
