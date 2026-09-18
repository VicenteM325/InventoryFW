package com.vm325.inventory_back.imaging;

import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;

/**
 * Ajuste de imagen con únicamente primitivas del JDK (sin dependencias
 * nuevas): auto-niveles siempre, luego brillo/contraste, luego nitidez. Cada
 * paso es una identidad cuando no tiene nada que hacer, así que llamar con
 * {@link EnhancementSettings#DEFAULT} es literalmente "solo auto-niveles +
 * la nitidez leve por defecto", sin una rama de código separada.
 */
@Service
public class ImageEnhancementServiceImpl implements ImageEnhancementService {

    private static final double AUTO_LEVELS_LOW_PERCENTILE = 0.01;
    private static final double AUTO_LEVELS_HIGH_PERCENTILE = 0.99;
    private static final int AUTO_LEVELS_MIN_RANGE = 200;

    @Override
    public BufferedImage enhance(BufferedImage source, EnhancementSettings settings) {
        BufferedImage result = applyAutoLevels(source);
        result = applyBrightnessContrast(result, settings.brightness(), settings.contrast());
        result = applySharpen(result, settings.sharpness());
        return result;
    }

    private BufferedImage applyAutoLevels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

        int[] histogram = new int[256];
        for (int rgb : pixels) {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            int luma = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
            histogram[luma]++;
        }

        long total = (long) width * height;
        long lowTarget = Math.round(total * AUTO_LEVELS_LOW_PERCENTILE);
        long highTarget = Math.round(total * AUTO_LEVELS_HIGH_PERCENTILE);

        int p1 = percentileValue(histogram, lowTarget);
        int p99 = percentileValue(histogram, highTarget);

        if (p99 <= p1 || p99 - p1 >= AUTO_LEVELS_MIN_RANGE) {
            return image;
        }

        byte[] lut = new byte[256];
        for (int v = 0; v < 256; v++) {
            double scaled = (v - p1) * 255.0 / (p99 - p1);
            lut[v] = (byte) (int) Math.round(Math.max(0, Math.min(255, scaled)));
        }

        BufferedImageOp op = new LookupOp(new ByteLookupTable(0, lut), null);
        return op.filter(image, null);
    }

    private int percentileValue(int[] histogram, long targetCount) {
        long cumulative = 0;
        for (int i = 0; i < histogram.length; i++) {
            cumulative += histogram[i];
            if (cumulative >= targetCount) {
                return i;
            }
        }
        return 255;
    }

    private BufferedImage applyBrightnessContrast(BufferedImage image, int brightness, int contrast) {
        if (brightness == 0 && contrast == 0) {
            return image;
        }
        double signedContrast = contrast * 2.55;
        double contrastFactor = (259.0 * (signedContrast + 255)) / (255.0 * (259 - signedContrast));
        float scale = (float) contrastFactor;
        float offset = (float) (128 * (1 - contrastFactor) + brightness * 1.28);

        RescaleOp op = new RescaleOp(scale, offset, null);
        return op.filter(image, null);
    }

    private BufferedImage applySharpen(BufferedImage image, int sharpness) {
        if (sharpness == 0) {
            return image;
        }
        float amount = sharpness / 100f * 2.0f;
        float[] kernelData = {
                0, -amount, 0,
                -amount, 1 + 4 * amount, -amount,
                0, -amount, 0
        };
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernelData), ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }
}
