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
     * @param manualCorners si no es {@code null}, se usa para rectificar en
     *                       vez de la detección automática — esquinas
     *                       ajustadas a mano por el usuario, en el mismo
     *                       espacio de píxeles que devuelve
     *                       {@link #detectCorners}.
     * @throws IllegalArgumentException si los bytes no representan una
     *                                  imagen decodificable (formato no
     *                                  soportado o archivo corrupto), o si
     *                                  {@code rotationOverride} no es 0, 90,
     *                                  180 o 270.
     */
    BufferedImage cropAndFit(byte[] rawImage, CropSpec spec, EnhancementSettings settings, int rotationOverride,
                              DetectedCorners manualCorners);

    /**
     * Decodifica la imagen, corrige EXIF y aplica la rotación manual (igual
     * que {@link #cropAndFit}), pero se detiene ahí: devuelve esa imagen
     * normalizada junto con las esquinas detectadas automáticamente, sin
     * recortar ni escalar todavía — para que el llamador pueda mostrarle al
     * usuario dónde detectó el borde antes de comprometerse a generar el
     * documento final.
     */
    NormalizedImageWithCorners detectCorners(byte[] rawImage, CropSpec spec, int rotationOverride);
}
