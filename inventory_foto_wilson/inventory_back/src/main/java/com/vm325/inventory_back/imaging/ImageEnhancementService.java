package com.vm325.inventory_back.imaging;

import java.awt.image.BufferedImage;

/**
 * Ajuste de color/nitidez de la imagen ya recortada del DPI. Llamar con
 * {@link EnhancementSettings#DEFAULT} es el modo "automático"; no existe un
 * camino de código separado para automático vs. manual.
 */
public interface ImageEnhancementService {

    BufferedImage enhance(BufferedImage source, EnhancementSettings settings);
}
