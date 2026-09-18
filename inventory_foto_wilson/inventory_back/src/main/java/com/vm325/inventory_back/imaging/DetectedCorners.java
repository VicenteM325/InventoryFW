package com.vm325.inventory_back.imaging;

/**
 * Las 4 esquinas de una tarjeta detectada (o ajustada a mano), ordenadas
 * TL/TR/BR/BL, en el espacio de píxeles de la imagen normalizada (ver
 * {@link CornerPoint}). Tipo público compartido entre {@link CardDetectionService},
 * {@link ImageCropService} y las capas de servicio/controlador.
 */
public record DetectedCorners(CornerPoint topLeft, CornerPoint topRight, CornerPoint bottomRight, CornerPoint bottomLeft) {}
