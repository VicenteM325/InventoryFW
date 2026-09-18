package com.vm325.inventory_back.imaging;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Punto único donde se registran los {@link DocumentLayoutSpec} de cada
 * {@link DocumentType} soportado. Agregar el Proceso 3 (fotos tamaño
 * cédula/pasaporte) debería requerir únicamente añadir una entrada aquí,
 * sin tocar {@link ImageCropService} ni ningún {@link DocumentComposer}.
 */
@Component
public class DocumentLayoutRegistry {

    // Tamaño carta en puntos PDF (1 in = 72 pt): 8.5in x 11in.
    private static final double LETTER_WIDTH_PT = 612d;
    private static final double LETTER_HEIGHT_PT = 792d;
    private static final double MARGIN_PT = 36d; // 0.5 in

    // DPI/cédula de identidad estándar: 85.6mm x 53.98mm, recortado a
    // ~300dpi para calidad de impresión aceptable.
    private static final CropSpec DPI_CARD_CROP = new CropSpec(1011, 638);

    private final Map<DocumentType, DocumentLayoutSpec> specs = new EnumMap<>(DocumentType.class);

    public DocumentLayoutRegistry() {
        specs.put(DocumentType.DPI, new DocumentLayoutSpec(
                DocumentType.DPI,
                LETTER_WIDTH_PT,
                LETTER_HEIGHT_PT,
                MARGIN_PT,
                DPI_CARD_CROP,
                2,
                List.of()
        ));
        // DocumentType.CEDULA_PASAPORTE queda intencionalmente sin registrar:
        // es el Proceso 3, documentado pero fuera del alcance de esta fase.
    }

    public DocumentLayoutSpec getSpec(DocumentType type) {
        DocumentLayoutSpec spec = specs.get(type);
        if (spec == null) {
            throw new UnsupportedOperationException(
                    "No hay un DocumentLayoutSpec registrado para " + type +
                            " (probablemente un tipo de documento aún no implementado)");
        }
        return spec;
    }
}
