package com.vm325.inventory_back.services;

import com.vm325.inventory_back.imaging.DetectedCorners;
import com.vm325.inventory_back.imaging.EnhancementSettings;
import com.vm325.inventory_back.imaging.OutputFormat;
import org.springframework.web.multipart.MultipartFile;

/**
 * Único componente que conoce la regla de negocio del Proceso 2 (BPM):
 * "un DPI son 2 imágenes, anverso y reverso, en una hoja tamaño carta".
 * Delega el trabajo genérico de recorte y composición a
 * {@code com.vm325.inventory_back.imaging}.
 */
public interface DpiDocumentService {

    /**
     * @param clientName nombre del cliente, opcional, usado solo para el
     *                    mensaje de la notificación generada (no se persiste
     *                    ningún dato del documento en sí).
     * @param settings ajuste de color/nitidez, compartido entre anverso y
     *                 reverso (se fotografían en la misma sesión/luz).
     * @param frontRotation giro manual (0/90/180/270) para el anverso.
     * @param backRotation giro manual (0/90/180/270) para el reverso.
     * @param frontManualCorners esquinas ajustadas a mano para el anverso
     *                           (o {@code null} para usar detección automática).
     * @param backManualCorners esquinas ajustadas a mano para el reverso
     *                          (o {@code null} para usar detección automática).
     * @return el documento generado, listo para enviarse como descarga.
     */
    byte[] generate(MultipartFile front, MultipartFile back, OutputFormat format, String clientName,
                     EnhancementSettings settings, int frontRotation, int backRotation,
                     DetectedCorners frontManualCorners, DetectedCorners backManualCorners);

    /**
     * Procesa una sola imagen (recorte + mejora) y la devuelve como PNG,
     * para que el frontend la muestre antes de generar el documento final.
     * Reutiliza exactamente el mismo procesamiento que {@link #generate},
     * así que lo que se ve en la vista previa es lo que termina en el
     * documento.
     *
     * @param side  "anverso" o "reverso", solo para el mensaje de error.
     * @param manualCorners esquinas ajustadas a mano, o {@code null} para
     *                      usar detección automática.
     */
    byte[] previewImage(MultipartFile image, String side, EnhancementSettings settings, int rotation,
                         DetectedCorners manualCorners);

    /**
     * Detecta el borde de la tarjeta en una sola imagen sin recortarla
     * todavía, para que el frontend le muestre al usuario dónde se detectó
     * (y le permita ajustarlo a mano) antes de generar el documento final.
     *
     * @param side "anverso" o "reverso", solo para el mensaje de error.
     */
    DetectCornersResult detectCorners(MultipartFile image, String side, int rotation);
}
