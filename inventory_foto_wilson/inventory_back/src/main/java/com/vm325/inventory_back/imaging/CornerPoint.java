package com.vm325.inventory_back.imaging;

/**
 * Un punto en el espacio de píxeles de la imagen ya normalizada (EXIF
 * corregido y con la rotación manual aplicada), antes de recortar/escalar.
 * Tipo público — a diferencia de {@link OrderedCorners}, que es interno y
 * está atado a tipos de BoofCV — para que las esquinas puedan cruzar hacia
 * los servicios/controladores y el frontend.
 */
public record CornerPoint(double x, double y) {}
