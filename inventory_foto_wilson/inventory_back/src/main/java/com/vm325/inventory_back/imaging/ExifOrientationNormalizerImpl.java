package com.vm325.inventory_back.imaging;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class ExifOrientationNormalizerImpl implements ExifOrientationNormalizer {

    private static final Logger log = LoggerFactory.getLogger(ExifOrientationNormalizerImpl.class);

    @Override
    public BufferedImage normalize(byte[] rawImage, BufferedImage decoded) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(rawImage));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory == null || !directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return decoded;
            }
            int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            return switch (orientation) {
                case 3 -> ImageRotationUtil.rotateClockwise(decoded, 180);
                case 6 -> ImageRotationUtil.rotateClockwise(decoded, 90);
                case 8 -> ImageRotationUtil.rotateClockwise(decoded, 270);
                // 1 = normal; 2/4/5/7 son variantes espejadas que las cámaras
                // prácticamente no producen y se dejan sin corregir.
                default -> decoded;
            };
        } catch (ImageProcessingException | IOException | MetadataException e) {
            log.debug("No se pudo leer la orientación EXIF, se usa la imagen sin rotar: {}", e.getMessage());
            return decoded;
        }
    }
}
