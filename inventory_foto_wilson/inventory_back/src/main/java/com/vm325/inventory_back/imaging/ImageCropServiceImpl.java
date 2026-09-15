package com.vm325.inventory_back.imaging;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Corrige orientación EXIF, intenta detectar y enderezar automáticamente el
 * borde de la tarjeta con {@link CardDetectionService}, y si no encuentra un
 * contorno confiable cae al recorte centrado a la relación de aspecto
 * objetivo (Java2D/BufferedImage nativo del JDK, sin dependencias externas).
 */
@Service
public class ImageCropServiceImpl implements ImageCropService {

    private final ExifOrientationNormalizer exifOrientationNormalizer;
    private final CardDetectionService cardDetectionService;

    public ImageCropServiceImpl(ExifOrientationNormalizer exifOrientationNormalizer,
                                 CardDetectionService cardDetectionService) {
        this.exifOrientationNormalizer = exifOrientationNormalizer;
        this.cardDetectionService = cardDetectionService;
    }

    @Override
    public BufferedImage cropAndFit(byte[] rawImage, CropSpec spec) {
        BufferedImage source;
        try {
            source = ImageIO.read(new ByteArrayInputStream(rawImage));
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer la imagen: " + e.getMessage(), e);
        }
        if (source == null) {
            throw new IllegalArgumentException("El archivo no es una imagen válida o su formato no es soportado");
        }

        BufferedImage upright = exifOrientationNormalizer.normalize(rawImage, source);
        BufferedImage cropped = cardDetectionService.detectAndRectify(upright, spec)
                .orElseGet(() -> centerCropToAspectRatio(upright, spec.aspectRatio()));
        return scaleTo(cropped, spec.targetWidthPx(), spec.targetHeightPx());
    }

    private BufferedImage centerCropToAspectRatio(BufferedImage source, double targetAspectRatio) {
        int width = source.getWidth();
        int height = source.getHeight();
        double sourceAspectRatio = (double) width / (double) height;

        int cropWidth;
        int cropHeight;
        if (sourceAspectRatio > targetAspectRatio) {
            // La imagen fuente es más "ancha" que el objetivo: recortar los lados.
            cropHeight = height;
            cropWidth = (int) Math.round(height * targetAspectRatio);
        } else {
            // La imagen fuente es más "alta" que el objetivo: recortar arriba/abajo.
            cropWidth = width;
            cropHeight = (int) Math.round(width / targetAspectRatio);
        }

        int x = Math.max(0, (width - cropWidth) / 2);
        int y = Math.max(0, (height - cropHeight) / 2);
        // Asegurar que el recorte no se salga de los límites por redondeo.
        cropWidth = Math.min(cropWidth, width - x);
        cropHeight = Math.min(cropHeight, height - y);

        return source.getSubimage(x, y, cropWidth, cropHeight);
    }

    private BufferedImage scaleTo(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetWidth, targetHeight);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g.dispose();
        }
        return scaled;
    }
}
