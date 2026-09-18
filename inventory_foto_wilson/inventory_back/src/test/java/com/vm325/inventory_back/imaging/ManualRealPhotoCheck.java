package com.vm325.inventory_back.imaging;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Inspección manual del detector contra fotos reales de DPI — NO es un test
 * automatizado: el nombre deliberadamente no termina en Test/Tests/TestCase
 * para que quede fuera de los patrones que Surefire ejecuta por defecto
 * (nunca corre en {@code mvn test} ni en CI). Solo se ejecuta a propósito
 * con {@code -Dtest=ManualRealPhotoCheck}, apuntando via variable de entorno
 * a una carpeta local de fotos reales que nunca se commitea al repositorio
 * (contienen datos personales reales).
 */
class ManualRealPhotoCheck {

    @Test
    void inspectRealPhotos() throws IOException {
        String dir = System.getenv("DPI_MANUAL_PHOTOS_DIR");
        Assumptions.assumeTrue(dir != null && !dir.isBlank(),
                "DPI_MANUAL_PHOTOS_DIR no está definida; se omite (solo para inspección manual local)");

        CardDetectionServiceImpl service = new CardDetectionServiceImpl();
        CropSpec spec = new CropSpec(1011, 638);
        Path outDir = Path.of("target/manual-real-photo-check");
        Files.createDirectories(outDir);

        try (Stream<Path> files = Files.list(Path.of(dir))) {
            for (Path file : files.sorted().toList()) {
                BufferedImage photo = ImageIO.read(file.toFile());
                if (photo == null) {
                    continue;
                }
                Optional<BufferedImage> result = service.detectAndRectify(photo, spec);
                System.out.printf("%s -> %s%n", file.getFileName(), result.isPresent() ? "DETECTADO" : "FALLBACK");
                if (result.isPresent()) {
                    Path outFile = outDir.resolve(file.getFileName() + "_cropped.png");
                    ImageIO.write(result.get(), "png", outFile.toFile());
                }
            }
        }
    }
}
