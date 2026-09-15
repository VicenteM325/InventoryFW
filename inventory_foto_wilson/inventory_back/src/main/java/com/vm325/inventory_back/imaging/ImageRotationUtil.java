package com.vm325.inventory_back.imaging;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Rotación de imágenes en múltiplos de 90°, compartida entre la corrección
 * de orientación EXIF y la rotación manual que el usuario puede pedir desde
 * la vista previa cuando la detección automática no basta.
 */
final class ImageRotationUtil {

    private ImageRotationUtil() {
    }

    static BufferedImage rotateClockwise(BufferedImage source, int degrees) {
        int normalized = ((degrees % 360) + 360) % 360;
        if (normalized == 0) {
            return source;
        }
        if (normalized != 90 && normalized != 180 && normalized != 270) {
            throw new IllegalArgumentException("degrees debe ser 0, 90, 180 o 270");
        }

        int width = source.getWidth();
        int height = source.getHeight();
        boolean swapsDimensions = normalized != 180;
        int newWidth = swapsDimensions ? height : width;
        int newHeight = swapsDimensions ? width : height;

        int type = source.getType() == BufferedImage.TYPE_CUSTOM
                ? BufferedImage.TYPE_INT_ARGB
                : source.getType();
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, type);

        Graphics2D g = rotated.createGraphics();
        try {
            AffineTransform transform = new AffineTransform();
            switch (normalized) {
                case 90 -> {
                    transform.translate(newWidth, 0);
                    transform.rotate(Math.PI / 2);
                }
                case 180 -> {
                    transform.translate(newWidth, newHeight);
                    transform.rotate(Math.PI);
                }
                case 270 -> {
                    transform.translate(0, newHeight);
                    transform.rotate(-Math.PI / 2);
                }
                default -> throw new IllegalStateException("unreachable");
            }
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(source, transform, null);
        } finally {
            g.dispose();
        }
        return rotated;
    }
}
