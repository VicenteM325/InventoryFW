package com.vm325.inventory_back.services;

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
     * @return el documento generado, listo para enviarse como descarga.
     */
    byte[] generate(MultipartFile front, MultipartFile back, OutputFormat format, String clientName);
}
