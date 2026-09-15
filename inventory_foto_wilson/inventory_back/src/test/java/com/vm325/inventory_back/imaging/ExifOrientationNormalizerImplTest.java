package com.vm325.inventory_back.imaging;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifica la rotación aplicada por cada código EXIF usando una imagen
 * sintética 4x2 con una esquina de color distintivo (rojo en la esquina
 * superior izquierda), sin depender de ningún archivo de foto real.
 */
class ExifOrientationNormalizerImplTest {

    private final ExifOrientationNormalizerImpl normalizer = new ExifOrientationNormalizerImpl();

    @Test
    void normalize_sinDatosExif_devuelveLaImagenSinCambios() {
        BufferedImage source = redTopLeftImage();
        BufferedImage result = normalizer.normalize(new byte[]{1, 2, 3}, source);
        assertEquals(source, result);
    }

    @Test
    void rotateClockwise_90grados_moveLaEsquinaSuperiorIzquierdaALaSuperiorDerecha() {
        BufferedImage source = redTopLeftImage();
        BufferedImage rotated = ImageRotationUtil.rotateClockwise(source, 90);

        assertEquals(source.getHeight(), rotated.getWidth());
        assertEquals(source.getWidth(), rotated.getHeight());
        assertEquals(Color.RED.getRGB(), rotated.getRGB(rotated.getWidth() - 1, 0));
    }

    @Test
    void rotateClockwise_180grados_moveLaEsquinaSuperiorIzquierdaALaInferiorDerecha() {
        BufferedImage source = redTopLeftImage();
        BufferedImage rotated = ImageRotationUtil.rotateClockwise(source, 180);

        assertEquals(source.getWidth(), rotated.getWidth());
        assertEquals(source.getHeight(), rotated.getHeight());
        assertEquals(Color.RED.getRGB(), rotated.getRGB(rotated.getWidth() - 1, rotated.getHeight() - 1));
    }

    @Test
    void rotateClockwise_270grados_moveLaEsquinaSuperiorIzquierdaALaInferiorIzquierda() {
        BufferedImage source = redTopLeftImage();
        BufferedImage rotated = ImageRotationUtil.rotateClockwise(source, 270);

        assertEquals(source.getHeight(), rotated.getWidth());
        assertEquals(source.getWidth(), rotated.getHeight());
        assertEquals(Color.RED.getRGB(), rotated.getRGB(0, rotated.getHeight() - 1));
    }

    @Test
    void rotateClockwise_0grados_devuelveLaMismaInstancia() {
        BufferedImage source = redTopLeftImage();
        assertEquals(source, ImageRotationUtil.rotateClockwise(source, 0));
    }

    private static BufferedImage redTopLeftImage() {
        BufferedImage image = new BufferedImage(4, 2, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        image.setRGB(0, 0, Color.RED.getRGB());
        return image;
    }
}
