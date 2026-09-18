package com.vm325.inventory_back.services;

import com.vm325.inventory_back.imaging.DetectedCorners;

/**
 * Respuesta de detección de esquinas para una sola imagen: la vista previa
 * (reducida, solo para mostrar) y las esquinas detectadas, siempre expresadas
 * en el espacio de píxeles de la imagen a tamaño completo ({@code fullWidth}
 * x {@code fullHeight}), no el de la vista previa reducida — el frontend
 * debe escalarlas al tamaño en el que las dibuja.
 */
public record DetectCornersResult(
        boolean detected,
        int fullWidth,
        int fullHeight,
        int previewWidth,
        int previewHeight,
        String previewImageBase64,
        DetectedCorners corners
) {}
