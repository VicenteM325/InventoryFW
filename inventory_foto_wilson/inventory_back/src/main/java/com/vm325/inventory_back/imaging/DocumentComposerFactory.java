package com.vm325.inventory_back.imaging;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DocumentComposerFactory {

    private final Map<OutputFormat, DocumentComposer> composersByFormat;

    public DocumentComposerFactory(List<DocumentComposer> composers) {
        this.composersByFormat = new EnumMap<>(OutputFormat.class);
        for (DocumentComposer composer : composers) {
            composersByFormat.put(composer.getSupportedFormat(), composer);
        }
    }

    public DocumentComposer get(OutputFormat format) {
        DocumentComposer composer = composersByFormat.get(format);
        if (composer == null) {
            throw new IllegalArgumentException("Formato de documento no soportado: " + format);
        }
        return composer;
    }
}
