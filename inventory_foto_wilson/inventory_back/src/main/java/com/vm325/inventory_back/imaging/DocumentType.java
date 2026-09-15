package com.vm325.inventory_back.imaging;

/**
 * Tipo de documento que el motor de recorte/composición de imágenes puede
 * generar. Cada tipo tiene su propio {@link DocumentLayoutSpec} registrado
 * en {@link DocumentLayoutRegistry}.
 * <p>
 * DPI es el único implementado en esta fase (Proceso 2 del análisis BPM).
 * CEDULA_PASAPORTE queda declarado como marcador de posición para el
 * Proceso 3 (documentado pero fuera de alcance de esta fase): cuando se
 * implemente, solo debería requerir agregar su spec al registro y un
 * servicio de orquestación análogo a DpiDocumentService, reutilizando
 * ImageCropService y los DocumentComposer tal cual están.
 */
public enum DocumentType {
    DPI,
    CEDULA_PASAPORTE
}
