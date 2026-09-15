package com.vm325.inventory_back.imaging;

import java.util.List;

/**
 * Configuración de composición para un {@link DocumentType}: tamaño de
 * página de salida, márgenes, resolución objetivo de cada imagen recortada,
 * cuántas imágenes espera el documento y una etiqueta opcional por imagen
 * (p.ej. "ANVERSO"/"REVERSO"). Un {@link DocumentComposer} solo necesita
 * esta clase y una lista de imágenes ya recortadas para producir el
 * documento final; no conoce ninguna regla de negocio de "DPI" ni de
 * ningún otro tipo de documento.
 *
 * @param pageWidthPt  ancho de página en puntos PDF (1 in = 72 pt).
 * @param pageHeightPt alto de página en puntos PDF.
 * @param marginPt     margen uniforme alrededor del contenido, en puntos.
 * @param cropSpec     resolución objetivo (px) a la que se recorta/ajusta cada imagen.
 * @param imageCount   cantidad de imágenes que este layout espera recibir.
 * @param labels       etiqueta opcional por imagen, en el mismo orden (tamaño 0 o imageCount).
 */
public record DocumentLayoutSpec(
        DocumentType documentType,
        double pageWidthPt,
        double pageHeightPt,
        double marginPt,
        CropSpec cropSpec,
        int imageCount,
        List<String> labels
) {
    public DocumentLayoutSpec {
        if (!labels.isEmpty() && labels.size() != imageCount) {
            throw new IllegalArgumentException(
                    "labels debe estar vacío o tener exactamente imageCount elementos");
        }
    }
}
