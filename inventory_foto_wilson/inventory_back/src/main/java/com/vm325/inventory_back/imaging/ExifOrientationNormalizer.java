package com.vm325.inventory_back.imaging;

import java.awt.image.BufferedImage;

/**
 * Corrige la orientación de fotos tomadas con cámara/celular a partir del
 * metadato EXIF de orientación, para las cuatro rotaciones que realmente
 * producen las cámaras (normal, 180°, 90° y 270°). Nunca falla: cualquier
 * imagen sin EXIF legible se devuelve sin cambios.
 */
public interface ExifOrientationNormalizer {

    BufferedImage normalize(byte[] rawImage, BufferedImage decoded);
}
