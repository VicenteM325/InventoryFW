package com.vm325.inventory_back.imaging;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Casos sintéticos (dibujados con Graphics2D, sin depender de fotos reales)
 * para el detector de bordes de tarjeta con BoofCV.
 */
class CardDetectionServiceImplTest {

    private final CardDetectionServiceImpl service = new CardDetectionServiceImpl();
    private static final CropSpec TEST_SPEC = new CropSpec(200, 126); // misma relación ~1.587 que el DPI real

    @Test
    void detectAndRectify_tarjetaGiradaSobreFondoContrastante_seDetectaYEnderezaAlTamanoObjetivo() {
        BufferedImage photo = rotatedCardOnDarkBackground();

        Optional<BufferedImage> result = service.detectAndRectify(photo, TEST_SPEC);

        assertTrue(result.isPresent(), "Debería detectar la tarjeta clara sobre fondo oscuro contrastante");
        assertEquals(TEST_SPEC.targetWidthPx(), result.get().getWidth());
        assertEquals(TEST_SPEC.targetHeightPx(), result.get().getHeight());
    }

    @Test
    void detectAndRectify_rectanguloOcupaCasiTodoElCuadro_haceFallback() {
        BufferedImage photo = frameFillingCard();

        Optional<BufferedImage> result = service.detectAndRectify(photo, TEST_SPEC);

        assertTrue(result.isEmpty(), "Un contorno que ocupa casi toda la foto debe descartarse, no 'detectarse'");
    }

    @Test
    void detectAndRectify_imagenDeUnSoloColor_haceFallbackSinLanzarExcepcion() {
        BufferedImage photo = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();
        g.setColor(new Color(128, 128, 128));
        g.fillRect(0, 0, 400, 300);
        g.dispose();

        Optional<BufferedImage> result = service.detectAndRectify(photo, TEST_SPEC);

        assertTrue(result.isEmpty(), "Sin bordes visibles no hay nada que detectar");
    }

    private static BufferedImage rotatedCardOnDarkBackground() {
        int canvasSize = 600;
        BufferedImage photo = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, canvasSize, canvasSize);

        g.setColor(new Color(230, 230, 230));
        g.rotate(Math.toRadians(8), canvasSize / 2.0, canvasSize / 2.0);
        int cardWidth = 300;
        int cardHeight = 189; // ~1011/638
        g.fillRect((canvasSize - cardWidth) / 2, (canvasSize - cardHeight) / 2, cardWidth, cardHeight);
        g.dispose();
        return photo;
    }

    private static BufferedImage frameFillingCard() {
        int width = 502;
        int height = 317;
        BufferedImage photo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(230, 230, 230));
        g.fillRect(1, 1, width - 2, height - 2);
        g.dispose();
        return photo;
    }
}
