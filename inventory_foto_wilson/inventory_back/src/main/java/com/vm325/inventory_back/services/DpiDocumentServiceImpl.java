package com.vm325.inventory_back.services;

import com.vm325.inventory_back.enums.NotificationType;
import com.vm325.inventory_back.imaging.DocumentComposerFactory;
import com.vm325.inventory_back.imaging.DocumentLayoutRegistry;
import com.vm325.inventory_back.imaging.DocumentLayoutSpec;
import com.vm325.inventory_back.imaging.DocumentType;
import com.vm325.inventory_back.imaging.EnhancementSettings;
import com.vm325.inventory_back.imaging.ImageCropService;
import com.vm325.inventory_back.imaging.OutputFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DpiDocumentServiceImpl implements DpiDocumentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final ImageCropService imageCropService;
    private final DocumentComposerFactory documentComposerFactory;
    private final DocumentLayoutRegistry documentLayoutRegistry;
    private final NotificationService notificationService;

    @Override
    public byte[] generate(MultipartFile front, MultipartFile back, OutputFormat format, String clientName) {
        validateImage(front, "anverso");
        validateImage(back, "reverso");

        DocumentLayoutSpec spec = documentLayoutRegistry.getSpec(DocumentType.DPI);

        BufferedImage frontImage = readAndCrop(front, spec, "anverso");
        BufferedImage backImage = readAndCrop(back, spec, "reverso");

        byte[] document = documentComposerFactory.get(format)
                .compose(List.of(frontImage, backImage), spec);

        String subject = (clientName == null || clientName.isBlank()) ? "" : " para " + clientName.trim();
        notificationService.notify(
                NotificationType.DOCUMENTO_GENERADO,
                "Documento DPI generado" + subject,
                null,
                null,
                null
        );

        return document;
    }

    private BufferedImage readAndCrop(MultipartFile file, DocumentLayoutSpec spec, String label) {
        try {
            return imageCropService.cropAndFit(file.getBytes(), spec.cropSpec(), EnhancementSettings.DEFAULT, 0);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La imagen de " + label + " no es válida: " + e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pudo leer la imagen de " + label);
        }
    }

    private void validateImage(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe adjuntar la imagen de " + label);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La imagen de " + label + " debe ser JPEG o PNG");
        }
    }
}
