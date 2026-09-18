package com.vm325.inventory_back.services;

import com.vm325.inventory_back.enums.NotificationType;
import com.vm325.inventory_back.imaging.DetectedCorners;
import com.vm325.inventory_back.imaging.DocumentComposerFactory;
import com.vm325.inventory_back.imaging.DocumentLayoutRegistry;
import com.vm325.inventory_back.imaging.DocumentLayoutSpec;
import com.vm325.inventory_back.imaging.DocumentType;
import com.vm325.inventory_back.imaging.EnhancementSettings;
import com.vm325.inventory_back.imaging.ImageCropService;
import com.vm325.inventory_back.imaging.NormalizedImageWithCorners;
import com.vm325.inventory_back.imaging.OutputFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
public class DpiDocumentServiceImpl implements DpiDocumentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final ImageCropService imageCropService;
    private final DocumentComposerFactory documentComposerFactory;
    private final DocumentLayoutRegistry documentLayoutRegistry;
    private final NotificationService notificationService;

    private static final int MAX_PREVIEW_DIMENSION_PX = 1024;

    @Override
    public byte[] generate(MultipartFile front, MultipartFile back, OutputFormat format, String clientName,
                            EnhancementSettings settings, int frontRotation, int backRotation,
                            DetectedCorners frontManualCorners, DetectedCorners backManualCorners) {
        validateImage(front, "anverso");
        validateImage(back, "reverso");

        DocumentLayoutSpec spec = documentLayoutRegistry.getSpec(DocumentType.DPI);

        BufferedImage frontImage = readAndCrop(front, spec, "anverso", settings, frontRotation, frontManualCorners);
        BufferedImage backImage = readAndCrop(back, spec, "reverso", settings, backRotation, backManualCorners);

        byte[] document = documentComposerFactory.get(format)
                .compose(List.of(frontImage, backImage), spec);

        String subject = (clientName == null || clientName.isBlank()) ? "" : " para " + clientName.trim();
        notificationService.notify(
                NotificationType.DOCUMENTO_GENERADO,
                "Documento DPI generado" + subject,
                null,
                null,
                null
        );

        return document;
    }

    @Override
    public byte[] previewImage(MultipartFile image, String side, EnhancementSettings settings, int rotation,
                                DetectedCorners manualCorners) {
        validateImage(image, side);
        DocumentLayoutSpec spec = documentLayoutRegistry.getSpec(DocumentType.DPI);
        BufferedImage processed = readAndCrop(image, spec, side, settings, rotation, manualCorners);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(processed, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error codificando la vista previa a PNG", e);
        }
    }

    @Override
    public DetectCornersResult detectCorners(MultipartFile image, String side, int rotation) {
        validateImage(image, side);
        DocumentLayoutSpec spec = documentLayoutRegistry.getSpec(DocumentType.DPI);
        try {
            NormalizedImageWithCorners result = imageCropService.detectCorners(image.getBytes(), spec.cropSpec(), rotation);
            BufferedImage full = result.normalizedImage();
            BufferedImage preview = downscale(full, MAX_PREVIEW_DIMENSION_PX);
            return new DetectCornersResult(
                    result.corners().isPresent(),
                    full.getWidth(), full.getHeight(),
                    preview.getWidth(), preview.getHeight(),
                    "data:image/jpeg;base64," + encodeJpegBase64(preview),
                    result.corners().orElse(null));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La imagen de " + side + " no es válida: " + e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pudo leer la imagen de " + side);
        }
    }

    private BufferedImage downscale(BufferedImage source, int maxDimension) {
        int width = source.getWidth();
        int height = source.getHeight();
        double scale = Math.min(1.0, (double) maxDimension / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g.dispose();
        }
        return scaled;
    }

    private String encodeJpegBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Error codificando la vista previa a JPEG", e);
        }
    }

    private BufferedImage readAndCrop(MultipartFile file, DocumentLayoutSpec spec, String label,
                                       EnhancementSettings settings, int rotation, DetectedCorners manualCorners) {
        try {
            return imageCropService.cropAndFit(file.getBytes(), spec.cropSpec(), settings, rotation, manualCorners);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La imagen de " + label + " no es válida: " + e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pudo leer la imagen de " + label);
        }
    }

    private void validateImage(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe adjuntar la imagen de " + label);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La imagen de " + label + " debe ser JPEG o PNG");
        }
    }
}
