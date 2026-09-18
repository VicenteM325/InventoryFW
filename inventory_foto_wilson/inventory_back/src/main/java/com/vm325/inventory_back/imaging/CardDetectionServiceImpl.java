package com.vm325.inventory_back.imaging;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConfigLength;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Detección del borde de la tarjeta con BoofCV (100% Java, sin bindings
 * nativos). Reúne contornos candidatos desde varias fuentes/señales
 * independientes — no depende de que una sola elección de polaridad o un
 * solo tipo de umbral acierte siempre:
 * <ol>
 *     <li>Umbral de brillo local, en sus dos polaridades (tarjeta más clara
 *     u oscura que el fondo).</li>
 *     <li>Bordes por gradiente (Canny) — no depende del brillo relativo
 *     entre tarjeta y fondo.</li>
 *     <li>Baja saturación de color (una tarjeta blanca/pálida se distingue
 *     de un fondo de color intenso aunque tengan brillo parecido).</li>
 *     <li>Baja textura local (desviación estándar en una ventana): una
 *     superficie plástica lisa se distingue de una tela o material con
 *     textura visible incluso en zonas de un solo color.</li>
 * </ol>
 * Cada máscara binaria pasa por una apertura morfológica (erosionar y
 * dilatar) para separar la tarjeta de elementos de fondo del mismo tono que
 * apenas la tocan. Cada contorno candidato resultante (sin exigirle una
 * cantidad exacta de vértices) se reduce a su envolvente convexa y al
 * rectángulo de área mínima que la contiene, y se filtra por longitud,
 * "fracción de puntos del contorno cercanos a los 4 lados del rectángulo"
 * (descarta blobs de tarjeta fusionada con ruido de fondo), tamaño y
 * relación de aspecto. Se evalúan todos los candidatos de todas las fuentes
 * juntos y se elige el de mayor área entre los que pasan todos los filtros.
 * <p>
 * Nota honesta: esto mejora sustancialmente la robustez frente a fondos con
 * textura o patrones moderados (mesas, telas, superficies con motivos), pero
 * un fondo adversarial — un patrón de alto contraste con zonas de color y
 * brillo muy parecidos a los de la tarjeta en un área considerable — puede
 * seguir sin detectarse por ninguna de estas señales clásicas; en ese caso
 * el llamador cae al recorte centrado de respaldo.
 */
@Service
public class CardDetectionServiceImpl implements CardDetectionService {

    private static final Logger log = LoggerFactory.getLogger(CardDetectionServiceImpl.class);

    private static final double MIN_AREA_FRACTION = 0.15;
    private static final double MAX_AREA_FRACTION = 0.98;
    private static final double ASPECT_RATIO_TOLERANCE = 0.20;
    private static final double MIN_INLIER_FRACTION = 0.75;
    private static final double INLIER_TOLERANCE_FRACTION = 0.015;
    private static final double MIN_CONTOUR_LENGTH_FRACTION = 0.25;

    private static final float CANNY_LOW_THRESHOLD = 0.1f;
    private static final float CANNY_HIGH_THRESHOLD = 0.3f;
    private static final int EDGE_DILATION_RADIUS = 2;
    private static final int OPENING_RADIUS = 5;
    private static final float SATURATION_THRESHOLD = 0.15f;
    private static final float BRIGHTNESS_THRESHOLD = 0.35f;
    private static final double TEXTURE_WINDOW_FRACTION = 0.01;
    private static final double TEXTURE_STDDEV_THRESHOLD = 15.0;

    @Override
    public Optional<BufferedImage> detectAndRectify(BufferedImage source, CropSpec spec) {
        try {
            GrayU8 gray = ConvertBufferedImage.convertFromSingle(source, null, GrayU8.class);
            int width = source.getWidth();
            int height = source.getHeight();
            double minContourPoints = Math.hypot(width, height) * MIN_CONTOUR_LENGTH_FRACTION;

            List<OrderedCorners> candidates = new ArrayList<>();
            candidates.addAll(candidatesFrom(localMeanBinary(gray, false), minContourPoints));
            candidates.addAll(candidatesFrom(localMeanBinary(gray, true), minContourPoints));
            candidates.addAll(candidatesFrom(cannyEdgeBinary(gray), minContourPoints));
            candidates.addAll(candidatesFrom(lowSaturationBinary(source), minContourPoints));
            candidates.addAll(candidatesFrom(lowTextureBinary(gray), minContourPoints));

            OrderedCorners best = pickBest(candidates, width, height, spec.aspectRatio());
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

    private GrayU8 localMeanBinary(GrayU8 gray, boolean inverted) {
        GrayU8 input = inverted ? invert(gray) : gray;
        GrayU8 binary = GThresholdImageOps.localMean(input, null,
                ConfigLength.relative(0.07, 25), 1.0, true, null, null, null);
        return opening(binary);
    }

    private GrayU8 cannyEdgeBinary(GrayU8 gray) {
        GrayU8 edgeImage = gray.createSameShape();
        CannyEdge<GrayU8, GrayS16> canny = FactoryEdgeDetectors.canny(2, true, true, GrayU8.class, GrayS16.class);
        canny.process(gray, CANNY_LOW_THRESHOLD, CANNY_HIGH_THRESHOLD, edgeImage);
        return BinaryImageOps.dilate8(edgeImage, EDGE_DILATION_RADIUS, null);
    }

    /**
     * Máscara de "objeto poco saturado y no oscuro": una tarjeta plástica
     * blanca/pálida tiene baja saturación de color, a diferencia de una
     * tela o superficie de color intenso, incluso cuando ambas tienen un
     * brillo (luminancia) parecido — es una señal distinta e independiente
     * del brillo, útil quando el fondo tiene zonas de brillo similar al de
     * la tarjeta pero de un color notoriamente distinto.
     */
    private GrayU8 lowSaturationBinary(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        GrayU8 mask = new GrayU8(width, height);
        float[] hsb = new float[3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = source.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                java.awt.Color.RGBtoHSB(r, g, b, hsb);
                boolean cardLike = hsb[1] < SATURATION_THRESHOLD && hsb[2] > BRIGHTNESS_THRESHOLD;
                mask.set(x, y, cardLike ? 1 : 0);
            }
        }
        return opening(mask);
    }

    /**
     * Máscara de "región lisa" vía desviación estándar local (calculada con
     * imágenes integrales, O(1) por píxel): el plástico de una tarjeta es
     * liso, mientras que una tela o superficie con textura tiene variación
     * local incluso en sus zonas de un solo color — señal independiente
     * tanto del brillo como de la saturación.
     */
    private GrayU8 lowTextureBinary(GrayU8 gray) {
        int width = gray.width;
        int height = gray.height;
        long[][] sum = new long[height + 1][width + 1];
        long[][] sumSq = new long[height + 1][width + 1];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long v = gray.get(x, y);
                sum[y + 1][x + 1] = v + sum[y][x + 1] + sum[y + 1][x] - sum[y][x];
                sumSq[y + 1][x + 1] = v * v + sumSq[y][x + 1] + sumSq[y + 1][x] - sumSq[y][x];
            }
        }

        int windowRadius = Math.max(3, (int) (Math.min(width, height) * TEXTURE_WINDOW_FRACTION));
        GrayU8 mask = new GrayU8(width, height);
        for (int y = 0; y < height; y++) {
            int y0 = Math.max(0, y - windowRadius);
            int y1 = Math.min(height - 1, y + windowRadius);
            for (int x = 0; x < width; x++) {
                int x0 = Math.max(0, x - windowRadius);
                int x1 = Math.min(width - 1, x + windowRadius);
                long count = (long) (y1 - y0 + 1) * (x1 - x0 + 1);
                long s = sum[y1 + 1][x1 + 1] - sum[y0][x1 + 1] - sum[y1 + 1][x0] + sum[y0][x0];
                long sq = sumSq[y1 + 1][x1 + 1] - sumSq[y0][x1 + 1] - sumSq[y1 + 1][x0] + sumSq[y0][x0];
                double mean = (double) s / count;
                double variance = Math.max(0, (double) sq / count - mean * mean);
                mask.set(x, y, Math.sqrt(variance) < TEXTURE_STDDEV_THRESHOLD ? 1 : 0);
            }
        }
        return opening(mask);
    }

    /**
     * "Opening" morfológico (erosionar y luego dilatar por el mismo radio):
     * corta puentes delgados entre la tarjeta y ruido de fondo del mismo
     * tono que apenas la toca, y luego le devuelve a la tarjeta su tamaño y
     * borde limpio original — a diferencia de erosionar sin más, que la
     * deja más chica y con un contorno más irregular.
     */
    private GrayU8 opening(GrayU8 binary) {
        GrayU8 eroded = BinaryImageOps.erode8(binary, OPENING_RADIUS, null);
        return BinaryImageOps.dilate8(eroded, OPENING_RADIUS, null);
    }

    /**
     * Qué fracción de los puntos del contorno original caen cerca de
     * alguno de los 4 lados del rectángulo candidato. Un contorno limpio de
     * tarjeta tiene casi el 100% de sus puntos sobre esos 4 lados; un blob
     * "tarjeta fusionada con ruido de fondo" tiene una porción de puntos
     * lejos de los 4 lados (trazando el ruido pegado), lo que esta métrica
     * penaliza de forma más directa que solo comparar áreas.
     */
    private double contourInlierFraction(List<Point2D_F64> contourPoints, OrderedCorners rect) {
        Point2D_F64[] corners = {rect.topLeft(), rect.topRight(), rect.bottomRight(), rect.bottomLeft()};
        double perimeter = 0;
        for (int i = 0; i < 4; i++) {
            perimeter += distance(corners[i], corners[(i + 1) % 4]);
        }
        double tolerance = Math.max(3.0, perimeter * INLIER_TOLERANCE_FRACTION);

        int inliers = 0;
        for (Point2D_F64 p : contourPoints) {
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                minDist = Math.min(minDist, distanceToSegment(p, corners[i], corners[(i + 1) % 4]));
            }
            if (minDist <= tolerance) {
                inliers++;
            }
        }
        return (double) inliers / contourPoints.size();
    }

    private double distanceToSegment(Point2D_F64 p, Point2D_F64 a, Point2D_F64 b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double lengthSq = dx * dx + dy * dy;
        double t = lengthSq == 0 ? 0 : ((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSq;
        t = Math.max(0, Math.min(1, t));
        double projX = a.x + t * dx;
        double projY = a.y + t * dy;
        return Math.hypot(p.x - projX, p.y - projY);
    }

    private double distance(Point2D_F64 a, Point2D_F64 b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
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
     * Extrae los contornos externos de una imagen binaria y reduce cada uno
     * (sin exigirle una cantidad exacta de vértices) a un rectángulo de
     * área mínima candidato, descartando los demasiado cortos para ser el
     * borde de una tarjeta fotografiada con un mínimo de encuadre.
     */
    private List<OrderedCorners> candidatesFrom(GrayU8 binary, double minContourPoints) {
        GrayS32 label = new GrayS32(binary.width, binary.height);
        List<Contour> contours = BinaryImageOps.contour(binary, ConnectRule.EIGHT, label);

        List<OrderedCorners> result = new ArrayList<>();
        for (Contour contour : contours) {
            List<Point2D_I32> externalPoints = contour.external;
            if (externalPoints.size() < minContourPoints) {
                continue;
            }

            List<Point2D_F64> pointsF64 = new ArrayList<>(externalPoints.size());
            for (Point2D_I32 p : externalPoints) {
                pointsF64.add(new Point2D_F64(p.x, p.y));
            }

            List<Point2D_F64> hull = ConvexHullUtil.convexHull(pointsF64);
            if (hull.size() < 3) {
                continue;
            }

            OrderedCorners rect = MinimumAreaRectangle.compute(hull);
            double rectArea = rect.areaSimple();
            if (rectArea <= 0) {
                continue;
            }

            double inlierFraction = contourInlierFraction(pointsF64, rect);
            if (inlierFraction < MIN_INLIER_FRACTION) {
                continue;
            }

            result.add(rect);
        }
        return result;
    }

    private OrderedCorners pickBest(List<OrderedCorners> candidates, int frameWidth, int frameHeight,
                                     double targetAspectRatio) {
        double frameArea = (double) frameWidth * frameHeight;
        OrderedCorners best = null;
        double bestArea = -1;

        for (OrderedCorners candidate : candidates) {
            double areaFraction = candidate.areaSimple() / frameArea;
            if (areaFraction < MIN_AREA_FRACTION || areaFraction > MAX_AREA_FRACTION) {
                continue;
            }
            if (!matchesTargetAspectRatio(candidate.aspectRatio(), targetAspectRatio)) {
                continue;
            }
            if (candidate.areaSimple() > bestArea) {
                bestArea = candidate.areaSimple();
                best = candidate;
            }
        }
        return best;
    }

    private boolean matchesTargetAspectRatio(double ratio, double targetAspectRatio) {
        boolean matchesDirect = Math.abs(ratio / targetAspectRatio - 1) <= ASPECT_RATIO_TOLERANCE;
        boolean matchesRotated = Math.abs((1 / ratio) / targetAspectRatio - 1) <= ASPECT_RATIO_TOLERANCE;
        return matchesDirect || matchesRotated;
    }

    private Optional<BufferedImage> rectify(BufferedImage source, OrderedCorners corners, CropSpec spec) {
        Planar<GrayF32> color = ConvertBufferedImage.convertFromPlanar(source, null, true, GrayF32.class);
        RemovePerspectiveDistortion<Planar<GrayF32>> remover = new RemovePerspectiveDistortion<>(
                spec.targetWidthPx(), spec.targetHeightPx(), ImageType.pl(3, GrayF32.class));

        boolean ok = remover.apply(color, corners.topLeft(), corners.topRight(), corners.bottomRight(), corners.bottomLeft());
        if (!ok) {
            return Optional.empty();
        }

        Planar<GrayF32> output = remover.getOutput();
        BufferedImage rectified = ConvertBufferedImage.convertTo_F32(output, null, true);
        return Optional.of(rectified);
    }
}
