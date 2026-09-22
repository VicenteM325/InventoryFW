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

    /**
     * Detecta las 4 esquinas de la tarjeta, sin rectificarla todavía —
     * separado de {@link #rectify} para que el llamador pueda mostrarle las
     * esquinas detectadas al usuario (p.ej. para ajustarlas a mano) antes de
     * comprometerse a recortar.
     */
    Optional<DetectedCorners> detectCorners(BufferedImage source, CropSpec spec);

    /**
     * Endereza por perspectiva la tarjeta usando las esquinas dadas —
     * detectadas automáticamente o ajustadas a mano por el usuario. Comparte
     * la misma matemática de rectificación que {@link #detectAndRectify},
     * sin duplicarla.
     */
    Optional<BufferedImage> rectify(BufferedImage source, DetectedCorners corners, CropSpec spec);

    default Optional<BufferedImage> detectAndRectify(BufferedImage source, CropSpec spec) {
        return detectCorners(source, spec).flatMap(corners -> rectify(source, corners, spec));
    }
}
