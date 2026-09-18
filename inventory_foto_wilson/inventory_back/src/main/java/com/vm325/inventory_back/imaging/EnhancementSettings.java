package com.vm325.inventory_back.imaging;

/**
 * Parámetros de ajuste de imagen para el documento DPI. {@link #DEFAULT} es
 * literalmente el modo "automático": no hay una rama de código separada
 * para automático vs. manual, solo distintos valores de este mismo record.
 */
public record EnhancementSettings(int brightness, int contrast, int sharpness) {

    public static final EnhancementSettings DEFAULT = new EnhancementSettings(0, 0, 15);

    public EnhancementSettings {
        if (brightness < -100 || brightness > 100) {
            throw new IllegalArgumentException("brightness debe estar entre -100 y 100");
        }
        if (contrast < -100 || contrast > 100) {
            throw new IllegalArgumentException("contrast debe estar entre -100 y 100");
        }
        if (sharpness < 0 || sharpness > 100) {
            throw new IllegalArgumentException("sharpness debe estar entre 0 y 100");
        }
    }
}
