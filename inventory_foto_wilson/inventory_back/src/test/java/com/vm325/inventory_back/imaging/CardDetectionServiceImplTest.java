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
    void detectAndRectify_tarjetaSobreFondoConPatron_seDetectaIgual() {
        // Caso de regresión: un fondo con patrón de alto contraste (como una
        // tela con lunares) rompía la estrategia anterior basada en brillo,
        // porque el patrón generaba tanto o más contraste que el borde real
        // de la tarjeta. La detección por bordes (Canny) + rectangularidad
        // no depende del brillo y debe encontrar la tarjeta igual.
        BufferedImage photo = rotatedCardOnPatternedBackground();

        Optional<BufferedImage> result = service.detectAndRectify(photo, TEST_SPEC);

        assertTrue(result.isPresent(), "Debería detectar la tarjeta a pesar del patrón de fondo");
        assertEquals(TEST_SPEC.targetWidthPx(), result.get().getWidth());
        assertEquals(TEST_SPEC.targetHeightPx(), result.get().getHeight());
    }

    @Test
    void detectAndRectify_tarjetaOcupaCasiTodoElCuadro_seDetectaIgual() {
        // Una tarjeta que llena casi todo el encuadre (foto ya bien
        // recortada, o tomada muy de cerca) es un caso legítimo, no un
        // artefacto de detección fallida: debe aceptarse igual, no
        // descartarse solo por ser grande.
        BufferedImage photo = frameFillingCard();

        Optional<BufferedImage> result = service.detectAndRectify(photo, TEST_SPEC);

        assertTrue(result.isPresent(), "Una tarjeta que ocupa casi todo el encuadre debe detectarse igual");
        assertEquals(TEST_SPEC.targetWidthPx(), result.get().getWidth());
        assertEquals(TEST_SPEC.targetHeightPx(), result.get().getHeight());
    }

    @Test
    void detectAndRectify_blobMasGrandeYSucioCompiteConTarjetaLimpia_ganaLaTarjeta() {
        // Caso de regresión: pickBest() elegía antes el candidato de MAYOR
        // ÁREA entre los que pasaban los filtros, sin importar qué tan
        // "limpio" (rectangular, con el contorno pegado a sus 4 lados) fuera
        // cada uno. Aquí un blob de fondo más grande pero con un borde
        // irregular (recorte en zigzag, rectangularidad baja) compite con la
        // tarjeta real, más chica pero perfectamente rectangular. El puntaje
        // compuesto (que pesa rectangularidad e inlier fraction, no solo
        // área) debe preferir la tarjeta real. Se verifica marcando el
        // centro de la tarjeta real con un cuadrado negro: si el recorte
        // final es oscuro en el centro, se recortó la tarjeta correcta: si
        // el blob grande hubiera ganado, el centro sería del mismo gris
        // uniforme que el resto del blob.
        BufferedImage photo = largeJaggedBlobCompetingWithCleanCard();

        Optional<BufferedImage> result = service.detectAndRectify(photo, TEST_SPEC);

        assertTrue(result.isPresent(), "Debería encontrar un candidato válido");
        BufferedImage cropped = result.get();
        int centerX = cropped.getWidth() / 2;
        int centerY = cropped.getHeight() / 2;
        int centerGray = new Color(cropped.getRGB(centerX, centerY)).getRed();
        assertTrue(centerGray < 100,
                "El centro del recorte debería ser el marcador oscuro de la tarjeta real, no el gris uniforme del blob grande (fue " + centerGray + ")");
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

    private static BufferedImage rotatedCardOnPatternedBackground() {
        int canvasSize = 600;
        BufferedImage photo = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();

        g.setColor(new Color(40, 90, 200));
        g.fillRect(0, 0, canvasSize, canvasSize);
        g.setColor(new Color(235, 235, 235));
        int dotSpacing = 40;
        int dotDiameter = 28;
        for (int x = 0; x < canvasSize; x += dotSpacing) {
            for (int y = 0; y < canvasSize; y += dotSpacing) {
                g.fillOval(x, y, dotDiameter, dotDiameter);
            }
        }

        g.setColor(Color.BLACK);
        g.rotate(Math.toRadians(20), canvasSize / 2.0, canvasSize / 2.0);
        int cardWidth = 340;
        int cardHeight = 215; // ~1011/638
        g.fillRect((canvasSize - cardWidth) / 2, (canvasSize - cardHeight) / 2, cardWidth, cardHeight);
        g.dispose();
        return photo;
    }

    private static BufferedImage largeJaggedBlobCompetingWithCleanCard() {
        int canvasSize = 650;
        BufferedImage photo = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, canvasSize, canvasSize);
        g.setColor(new Color(230, 230, 230));

        // Blob grande con la relación de aspecto objetivo pero una esquina
        // cortada en diagonal (pentágono, no un rectángulo limpio): sigue
        // siendo un candidato válido (pasa los filtros de área e inlier
        // fraction) pero con rectangularidad e inlier fraction notoriamente
        // peores que una tarjeta limpia — con un área bastante mayor a la
        // de la tarjeta real (ambas dentro del rango válido 15%-98% del
        // cuadro), para que "el área más grande gana" lo hubiera elegido
        // antes.
        int blobX = 20, blobY = 20, blobWidth = 520, blobHeight = 328; // ~1011/638
        int chamfer = 150;
        int[] xs = {blobX, blobX + blobWidth - chamfer, blobX + blobWidth, blobX + blobWidth, blobX};
        int[] ys = {blobY, blobY, blobY + chamfer, blobY + blobHeight, blobY + blobHeight};
        g.fillPolygon(xs, ys, xs.length);

        // Tarjeta real: más chica pero aun así un candidato válido (>15%
        // del cuadro), perfectamente rectangular, con un marcador negro en
        // el centro para poder verificar cuál candidato terminó siendo el
        // recortado.
        int cardX = 20, cardY = 400, cardWidth = 340, cardHeight = 214; // ~1011/638
        g.fillRect(cardX, cardY, cardWidth, cardHeight);
        g.setColor(Color.BLACK);
        int markerSize = 40;
        g.fillRect(cardX + cardWidth / 2 - markerSize / 2, cardY + cardHeight / 2 - markerSize / 2, markerSize, markerSize);

        g.dispose();
        addNoise(photo);
        return photo;
    }

    private static BufferedImage frameFillingCard() {
        // Canvas grande con un borde de 1px: dejar un margen holgado por
        // encima de MAX_AREA_FRACTION (98%) para que el resultado no dependa
        // de pequeñas variaciones de área introducidas por la apertura
        // morfológica o por el ajuste de envolvente convexa/rectángulo de
        // área mínima (a diferencia de un canvas chico, donde 1px de borde
        // deja un margen de menos del 1% sobre el umbral).
        int width = 1000;
        int height = 631;
        BufferedImage photo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(230, 230, 230));
        g.fillRect(1, 1, width - 2, height - 2);
        g.dispose();
        // Un poco de ruido: una imagen perfectamente uniforme (sin ninguna
        // variación de píxel a píxel) puede producir empates degenerados en
        // el umbral de brillo local ("valor == media local" cuenta como
        // primer plano en ambos lados), algo que una foto real con ruido de
        // cámara nunca produce.
        addNoise(photo);
        return photo;
    }

    private static void addNoise(BufferedImage image) {
        java.util.Random random = new java.util.Random(42);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color c = new Color(image.getRGB(x, y));
                int delta = random.nextInt(21) - 10;
                int gray = Math.max(0, Math.min(255, c.getRed() + delta));
                image.setRGB(x, y, new Color(gray, gray, gray).getRGB());
            }
        }
    }
}
