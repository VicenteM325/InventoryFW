package com.vm325.inventory_back.imaging;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.shapes.polygon.DetectPolygonBinaryGrayRefine;
import boofcv.factory.shape.ConfigPolygonDetector;
import boofcv.factory.shape.FactoryShapeDetector;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConfigLength;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.Polygon2D_F64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

/**
 * Detección de tarjeta con BoofCV (100% Java, sin bindings nativos): busca
 * un cuadrilátero convexo en la imagen, filtra candidatos por tamaño y
 * relación de aspecto, y endereza el mejor candidato por perspectiva
 * directamente al tamaño objetivo del {@link CropSpec}.
 */
@Service
public class CardDetectionServiceImpl implements CardDetectionService {

    private static final Logger log = LoggerFactory.getLogger(CardDetectionServiceImpl.class);

    private static final double MIN_AREA_FRACTION = 0.15;
    private static final double MAX_AREA_FRACTION = 0.98;
    private static final double ASPECT_RATIO_TOLERANCE = 0.20;
    private static final double BORDER_STRIP_FRACTION = 0.03;

    @Override
    public Optional<BufferedImage> detectAndRectify(BufferedImage source, CropSpec spec) {
        try {
            GrayU8 gray = ConvertBufferedImage.convertFromSingle(source, null, GrayU8.class);

            // DetectPolygonFromContour asume documentos oscuros sobre fondo
            // claro y descarta explícitamente cualquier "blob blanco"
            // (más claro que su entorno) sin importar cómo se construya el
            // binario de entrada. Una tarjeta más clara que el fondo (el
            // caso típico: DPI claro sobre mesa/mano oscuras) se invierte
            // primero para que el detector la vea como "oscura sobre clara".
            GrayU8 detectionGray = isCenterBrighterThanBorder(gray) ? invert(gray) : gray;

            // Umbral local (no global): una foto real tiene iluminación
            // despareja (sombras, brillo del laminado) que con un único
            // umbral global fragmenta el contorno de la tarjeta en varios
            // pedazos, mientras que detalles pequeños de alto contraste
            // (texto, chip, iconos) sí quedan limpios y se cuelan como
            // falsos candidatos. Un umbral local con una ventana más grande
            // que esos detalles pero más chica que la tarjeta evita eso.
            // Relativo al tamaño de la imagen (no fijo en píxeles) para que
            // escale con fotos de distinta resolución.
            GrayU8 binary = GThresholdImageOps.localMean(detectionGray, null,
                    ConfigLength.relative(0.07, 25), 1.0, true, null, null, null);

            ConfigPolygonDetector config = new ConfigPolygonDetector(4, 4);
            DetectPolygonBinaryGrayRefine<GrayU8> detector = FactoryShapeDetector.polygon(config, GrayU8.class);
            detector.process(detectionGray, binary);

            List<Polygon2D_F64> candidates = detector.getPolygons(null, null);
            Polygon2D_F64 best = pickBestCandidate(candidates, source.getWidth(), source.getHeight(), spec.aspectRatio());
            if (best == null) {
                log.debug("No se encontró un contorno de tarjeta confiable, se usará el recorte de respaldo");
                return Optional.empty();
            }

            return rectify(source, best, spec);
        } catch (Exception e) {
            log.debug("Falla detectando/enderezando la tarjeta, se usará el recorte de respaldo: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private GrayU8 invert(GrayU8 gray) {
        GrayU8 inverted = new GrayU8(gray.width, gray.height);
        for (int y = 0; y < gray.height; y++) {
            for (int x = 0; x < gray.width; x++) {
                inverted.set(x, y, 255 - gray.get(x, y));
            }
        }
        return inverted;
    }

    /**
     * Compara el brillo promedio de un marco delgado en el borde de la foto
     * contra el brillo promedio de una región central, para decidir si la
     * tarjeta (se asume razonablemente centrada) es más clara o más oscura
     * que el fondo, sin depender del valor exacto que devuelva Otsu.
     */
    private boolean isCenterBrighterThanBorder(GrayU8 gray) {
        int width = gray.width;
        int height = gray.height;
        int stripX = Math.max(1, (int) (width * BORDER_STRIP_FRACTION));
        int stripY = Math.max(1, (int) (height * BORDER_STRIP_FRACTION));

        long borderSum = 0;
        long borderCount = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < stripY; y++) {
                borderSum += gray.get(x, y);
                borderCount++;
            }
            for (int y = height - stripY; y < height; y++) {
                borderSum += gray.get(x, y);
                borderCount++;
            }
        }
        for (int y = stripY; y < height - stripY; y++) {
            for (int x = 0; x < stripX; x++) {
                borderSum += gray.get(x, y);
                borderCount++;
            }
            for (int x = width - stripX; x < width; x++) {
                borderSum += gray.get(x, y);
                borderCount++;
            }
        }

        int centerHalfWidth = Math.max(1, width / 10);
        int centerHalfHeight = Math.max(1, height / 10);
        int cx = width / 2;
        int cy = height / 2;
        long centerSum = 0;
        long centerCount = 0;
        for (int x = cx - centerHalfWidth; x < cx + centerHalfWidth; x++) {
            for (int y = cy - centerHalfHeight; y < cy + centerHalfHeight; y++) {
                centerSum += gray.get(x, y);
                centerCount++;
            }
        }

        double borderAverage = borderCount == 0 ? 0 : (double) borderSum / borderCount;
        double centerAverage = centerCount == 0 ? 0 : (double) centerSum / centerCount;
        return centerAverage > borderAverage;
    }

    private Polygon2D_F64 pickBestCandidate(List<Polygon2D_F64> candidates, int frameWidth, int frameHeight,
                                             double targetAspectRatio) {
        double frameArea = (double) frameWidth * frameHeight;
        Polygon2D_F64 best = null;
        double bestArea = -1;

        for (Polygon2D_F64 polygon : candidates) {
            if (polygon.size() != 4) {
                continue;
            }
            double area = polygon.areaSimple();
            double areaFraction = area / frameArea;
            if (areaFraction < MIN_AREA_FRACTION || areaFraction > MAX_AREA_FRACTION) {
                continue;
            }

            OrderedCorners corners = OrderedCorners.from(polygon);
            double ratio = corners.aspectRatio();
            boolean matchesDirect = Math.abs(ratio / targetAspectRatio - 1) <= ASPECT_RATIO_TOLERANCE;
            boolean matchesRotated = Math.abs((1 / ratio) / targetAspectRatio - 1) <= ASPECT_RATIO_TOLERANCE;
            if (!matchesDirect && !matchesRotated) {
                continue;
            }

            if (area > bestArea) {
                bestArea = area;
                best = polygon;
            }
        }
        return best;
    }

    private Optional<BufferedImage> rectify(BufferedImage source, Polygon2D_F64 polygon, CropSpec spec) {
        OrderedCorners corners = OrderedCorners.from(polygon);

        Planar<GrayF32> color = ConvertBufferedImage.convertFromPlanar(source, null, true, GrayF32.class);
        RemovePerspectiveDistortion<Planar<GrayF32>> remover = new RemovePerspectiveDistortion<>(
                spec.targetWidthPx(), spec.targetHeightPx(), ImageType.pl(3, GrayF32.class));

        boolean ok = remover.apply(color, corners.topLeft, corners.topRight, corners.bottomRight, corners.bottomLeft);
        if (!ok) {
            return Optional.empty();
        }

        Planar<GrayF32> output = remover.getOutput();
        BufferedImage rectified = ConvertBufferedImage.convertTo_F32(output, null, true);
        return Optional.of(rectified);
    }

    /**
     * Ordena las 4 esquinas de un cuadrilátero como TL/TR/BR/BL usando el
     * truco clásico de suma/diferencia de coordenadas, que funciona sin
     * importar el orden ni el sentido (horario/antihorario) con el que
     * BoofCV haya devuelto los vértices.
     */
    private static final class OrderedCorners {
        final Point2D_F64 topLeft;
        final Point2D_F64 topRight;
        final Point2D_F64 bottomRight;
        final Point2D_F64 bottomLeft;

        private OrderedCorners(Point2D_F64 topLeft, Point2D_F64 topRight,
                                Point2D_F64 bottomRight, Point2D_F64 bottomLeft) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomRight = bottomRight;
            this.bottomLeft = bottomLeft;
        }

        static OrderedCorners from(Polygon2D_F64 polygon) {
            Point2D_F64 tl = polygon.get(0);
            Point2D_F64 br = polygon.get(0);
            Point2D_F64 tr = polygon.get(0);
            Point2D_F64 bl = polygon.get(0);
            double minSum = Double.MAX_VALUE, maxSum = -Double.MAX_VALUE;
            double minDiff = Double.MAX_VALUE, maxDiff = -Double.MAX_VALUE;

            for (int i = 0; i < polygon.size(); i++) {
                Point2D_F64 p = polygon.get(i);
                double sum = p.x + p.y;
                double diff = p.y - p.x;
                if (sum < minSum) {
                    minSum = sum;
                    tl = p;
                }
                if (sum > maxSum) {
                    maxSum = sum;
                    br = p;
                }
                if (diff < minDiff) {
                    minDiff = diff;
                    tr = p;
                }
                if (diff > maxDiff) {
                    maxDiff = diff;
                    bl = p;
                }
            }
            return new OrderedCorners(tl, tr, br, bl);
        }

        double aspectRatio() {
            double topWidth = distance(topLeft, topRight);
            double bottomWidth = distance(bottomLeft, bottomRight);
            double leftHeight = distance(topLeft, bottomLeft);
            double rightHeight = distance(topRight, bottomRight);
            double width = (topWidth + bottomWidth) / 2.0;
            double height = (leftHeight + rightHeight) / 2.0;
            return width / height;
        }

        private static double distance(Point2D_F64 a, Point2D_F64 b) {
            double dx = a.x - b.x;
            double dy = a.y - b.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
}
