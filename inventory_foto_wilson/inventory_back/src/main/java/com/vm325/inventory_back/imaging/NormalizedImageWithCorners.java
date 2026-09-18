package com.vm325.inventory_back.imaging;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Resultado de {@link ImageCropService#detectCorners}: la imagen ya
 * normalizada (EXIF corregido + rotación manual aplicada, antes de
 * recortar/escalar) junto con las esquinas detectadas en ese mismo espacio
 * de píxeles, si se encontraron. El llamador (capa de servicio) muestra esta
 * imagen normalizada al usuario para que ajuste las esquinas a mano en el
 * mismo sistema de coordenadas que luego se usará para recortar.
 */
public record NormalizedImageWithCorners(BufferedImage normalizedImage, Optional<DetectedCorners> corners) {}
