package com.vm325.inventory_back.imaging;

import java.awt.image.BufferedImage;

/**
 * Motor de recorte y ajuste de imágenes, agnóstico de reglas de negocio: no
 * sabe qué es un "DPI" ni ningún otro tipo de documento, solo transforma
 * píxeles según una {@link CropSpec}. Esto es lo que lo hace reutilizable
 * para cualquier {@link DocumentType} futuro.
 */
public interface ImageCropService {

    /**
     * Decodifica una imagen cruda, corrige su orientación EXIF, aplica el
     * giro manual indicado por {@code rotationOverride} (0/90/180/270,
     * antes de cualquier detección — para cuando la geometría del recorte
     * automático deja la tarjeta con el lado correcto pero la orientación
     * de lectura girada, algo que la sola geometría no puede resolver por
     * sí sola), la recorta (por detección automática de borde o, si falla,
     * centrada) a la relación de aspecto de {@code spec}, la reescala a su
     * resolución objetivo y le aplica el ajuste de color/nitidez indicado
     * por {@code settings} ({@link EnhancementSettings#DEFAULT} es el modo
     * automático).
     *
     * @throws IllegalArgumentException si los bytes no representan una
     *                                  imagen decodificable (formato no
     *                                  soportado o archivo corrupto), o si
     *                                  {@code rotationOverride} no es 0, 90,
     *                                  180 o 270.
     */
    BufferedImage cropAndFit(byte[] rawImage, CropSpec spec, EnhancementSettings settings, int rotationOverride);
}
