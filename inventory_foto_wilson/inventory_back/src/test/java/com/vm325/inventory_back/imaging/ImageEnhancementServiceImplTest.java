package com.vm325.inventory_back.imaging;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageEnhancementServiceImplTest {

    private final ImageEnhancementServiceImpl service = new ImageEnhancementServiceImpl();

    @Test
    void enhance_gradienteDeBajoContraste_ampliaElRangoDeValores() {
        BufferedImage lowContrast = grayGradient(100, 150);

        BufferedImage result = service.enhance(lowContrast, EnhancementSettings.DEFAULT);

        int[] before = minMaxGray(lowContrast);
        int[] after = minMaxGray(result);
        int rangeBefore = before[1] - before[0];
        int rangeAfter = after[1] - after[0];
        assertTrue(rangeAfter > rangeBefore,
                "El rango de valores debería ampliarse: antes=" + rangeBefore + " despues=" + rangeAfter);
    }

    @Test
    void enhance_grisMedio_esInvarianteAlContraste() {
        BufferedImage solidGray = solidColor(128, 128, 128);

        for (int contrast : new int[]{-100, -50, 0, 50, 100}) {
            BufferedImage result = service.enhance(solidGray, new EnhancementSettings(0, contrast, 0));
            Color pixel = new Color(result.getRGB(2, 2));
            assertEquals(128, pixel.getRed(), "rojo con contraste=" + contrast);
            assertEquals(128, pixel.getGreen(), "verde con contraste=" + contrast);
            assertEquals(128, pixel.getBlue(), "azul con contraste=" + contrast);
        }
    }

    @Test
    void enhance_sharpnessCero_esUnaIdentidadCompleta() {
        BufferedImage edge = verticalHardEdge();

        BufferedImage result = service.enhance(edge, new EnhancementSettings(0, 0, 0));

        for (int x = 0; x < edge.getWidth(); x++) {
            assertEquals(edge.getRGB(x, 2), result.getRGB(x, 2), "columna x=" + x);
        }
    }

    @Test
    void enhance_sharpnessMayorQueCero_cambiaLosPixelesDelBorde() {
        // Salto moderado (no 0/255 máximo): con contraste extremo el
        // sobre-impulso de la nitidez se recorta (clamp) justo de vuelta al
        // valor original y ocultaría el cambio en la aserción. Se compara
        // contra la salida con nitidez=0 del mismo pipeline (no contra la
        // imagen cruda) para aislar exactamente el efecto de este paso.
        BufferedImage edge = moderateEdge();
        BufferedImage baseline = service.enhance(edge, new EnhancementSettings(0, 0, 0));
        BufferedImage sharpened = service.enhance(edge, new EnhancementSettings(0, 0, 50));

        boolean anyPixelChanged = false;
        for (int x = 0; x < edge.getWidth(); x++) {
            if (baseline.getRGB(x, 2) != sharpened.getRGB(x, 2)) {
                anyPixelChanged = true;
                break;
            }
        }
        assertTrue(anyPixelChanged, "La nitidez > 0 debería alterar al menos un píxel cerca del borde");
    }

    private static BufferedImage grayGradient(int fromGray, int toGray) {
        int width = 100;
        int height = 10;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            int gray = fromGray + (toGray - fromGray) * x / (width - 1);
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, new Color(gray, gray, gray).getRGB());
            }
        }
        return image;
    }

    private static BufferedImage solidColor(int r, int g, int b) {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        int rgb = new Color(r, g, b).getRGB();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    private static BufferedImage verticalHardEdge() {
        return verticalEdge(0, 255);
    }

    /**
     * Un salto moderado (20 a 235, no 0 a 255) para que el efecto de la
     * nitidez sea visible: con un salto máximo 0/255 el sobre-impulso del
     * kernel se recorta (clamp) exactamente de vuelta al valor original,
     * ocultando el cambio en la aserción. El rango (215) sigue siendo
     * suficiente para que auto-niveles no lo estire primero de vuelta a
     * 0/255 (el umbral de auto-niveles es 200).
     */
    private static BufferedImage moderateEdge() {
        return verticalEdge(20, 235);
    }

    private static BufferedImage verticalEdge(int fromGray, int toGray) {
        int width = 20;
        int height = 5;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            int gray = x < width / 2 ? fromGray : toGray;
            int rgb = new Color(gray, gray, gray).getRGB();
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    private static int[] minMaxGray(BufferedImage image) {
        int min = 255;
        int max = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int gray = rgb & 0xFF; // imagen en escala de grises: R=G=B
                min = Math.min(min, gray);
                max = Math.max(max, gray);
            }
        }
        return new int[]{min, max};
    }
}
