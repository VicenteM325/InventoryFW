package com.vm325.inventory_back.controllers;

import com.vm325.inventory_back.imaging.EnhancementSettings;
import com.vm325.inventory_back.imaging.OutputFormat;
import com.vm325.inventory_back.services.DpiDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private static final DateTimeFormatter FILENAME_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final DpiDocumentService dpiDocumentService;

    // Proceso 2 (BPM): generación automática de fotocopia de DPI a partir
    // de las fotos de anverso y reverso subidas por el Encargado (RF-05).
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @PostMapping(value = "/dpi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generateDpiDocument(
            @RequestParam("front") MultipartFile front,
            @RequestParam("back") MultipartFile back,
            @RequestParam(defaultValue = "PDF") String format,
            @RequestParam(required = false) String clientName,
            @RequestParam(defaultValue = "0") int brightness,
            @RequestParam(defaultValue = "0") int contrast,
            @RequestParam(defaultValue = "15") int sharpness,
            @RequestParam(defaultValue = "0") int frontRotation,
            @RequestParam(defaultValue = "0") int backRotation) {

        OutputFormat outputFormat = parseFormat(format);
        EnhancementSettings settings = new EnhancementSettings(brightness, contrast, sharpness);
        byte[] document = dpiDocumentService.generate(front, back, outputFormat, clientName,
                settings, frontRotation, backRotation);

        String filename = "dpi_" + LocalDateTime.now().format(FILENAME_TIMESTAMP)
                + "." + outputFormat.getFileExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(outputFormat.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(document);
    }

    // Vista previa de una sola imagen (recorte + ajuste) para que el
    // frontend la muestre antes de comprometerse a generar el documento
    // final; reutiliza el mismo servicio y por lo tanto el mismo
    // procesamiento que generateDpiDocument.
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @PostMapping(value = "/dpi/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> previewDpiImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("side") String side,
            @RequestParam(defaultValue = "0") int brightness,
            @RequestParam(defaultValue = "0") int contrast,
            @RequestParam(defaultValue = "15") int sharpness,
            @RequestParam(defaultValue = "0") int rotation) {

        String label = "front".equalsIgnoreCase(side) ? "anverso" : "reverso";
        EnhancementSettings settings = new EnhancementSettings(brightness, contrast, sharpness);
        byte[] png = dpiDocumentService.previewImage(image, label, settings, rotation);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    private OutputFormat parseFormat(String format) {
        try {
            return OutputFormat.valueOf(format.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Formato no soportado: " + format + " (use PDF o DOCX)");
        }
    }
}
