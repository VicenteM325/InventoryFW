package com.vm325.inventory_back.imaging;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Compone un documento final a partir de imágenes ya recortadas/ajustadas
 * por {@link ImageCropService} y un {@link DocumentLayoutSpec}. No conoce
 * ninguna regla de negocio de "DPI" ni de ningún otro {@link DocumentType}:
 * solo sabe acomodar imágenes en un documento del formato que soporta.
 */
public interface DocumentComposer {

    OutputFormat getSupportedFormat();

    /**
     * @param images ya recortadas/ajustadas, en el mismo orden que
     *               {@code spec.labels()} si esta no está vacía.
     */
    byte[] compose(List<BufferedImage> images, DocumentLayoutSpec spec);
}
