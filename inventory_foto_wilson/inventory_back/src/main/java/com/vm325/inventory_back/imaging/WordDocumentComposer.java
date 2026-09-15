package com.vm325.inventory_back.imaging;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Genera el documento en formato Word (.docx). Un XWPFDocument recién
 * creado ya usa tamaño carta por defecto (el estándar OOXML), consistente
 * con {@link DocumentLayoutSpec#pageWidthPt()}/{@code pageHeightPt()}
 * usados por {@link PdfDocumentComposer}.
 */
@Component
public class WordDocumentComposer implements DocumentComposer {

    private static final int EMU_PER_POINT = 12700; // 914400 EMU/in ÷ 72 pt/in

    @Override
    public OutputFormat getSupportedFormat() {
        return OutputFormat.DOCX;
    }

    @Override
    public byte[] compose(List<BufferedImage> images, DocumentLayoutSpec spec) {
        float contentWidthPt = (float) (spec.pageWidthPt() - 2 * spec.marginPt());
        boolean hasLabels = !spec.labels().isEmpty();

        try (XWPFDocument doc = new XWPFDocument()) {
            for (int i = 0; i < images.size(); i++) {
                BufferedImage image = images.get(i);

                if (hasLabels) {
                    XWPFParagraph labelParagraph = doc.createParagraph();
                    labelParagraph.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun labelRun = labelParagraph.createRun();
                    labelRun.setBold(true);
                    labelRun.setFontSize(12);
                    labelRun.setText(spec.labels().get(i));
                }

                float imageAspectRatio = (float) image.getWidth() / image.getHeight();
                float drawWidthPt = contentWidthPt;
                float drawHeightPt = drawWidthPt / imageAspectRatio;

                byte[] pngBytes = toPng(image);
                XWPFParagraph imageParagraph = doc.createParagraph();
                imageParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun imageRun = imageParagraph.createRun();
                try (ByteArrayInputStream imageStream = new ByteArrayInputStream(pngBytes)) {
                    imageRun.addPicture(
                            imageStream,
                            Document.PICTURE_TYPE_PNG,
                            "image-" + i + ".png",
                            Math.round(drawWidthPt * EMU_PER_POINT),
                            Math.round(drawHeightPt * EMU_PER_POINT)
                    );
                }

                if (i < images.size() - 1) {
                    doc.createParagraph();
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        } catch (InvalidFormatException e) {
            throw new IllegalStateException("Error generando el documento Word: formato de imagen inválido", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Error generando el documento Word", e);
        }
    }

    private byte[] toPng(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error codificando imagen a PNG", e);
        }
    }
}
