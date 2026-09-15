package com.vm325.inventory_back.imaging;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Detecta el contorno de una tarjeta (DPI u otro documento rígido) dentro de
 * una foto que puede incluir fondo alrededor (mesa, mano, etc.) y un ángulo
 * de inclinación arbitrario, y la endereza por perspectiva al tamaño
 * objetivo indicado por {@link CropSpec}.
 * <p>
 * No es una operación que deba poder fallar la generación del documento: si
 * no se encuentra un contorno confiable, el llamador debe usar un recorte de
 * respaldo (por ejemplo {@code centerCropToAspectRatio}).
 */
public interface CardDetectionService {

    Optional<BufferedImage> detectAndRectify(BufferedImage source, CropSpec spec);
}
