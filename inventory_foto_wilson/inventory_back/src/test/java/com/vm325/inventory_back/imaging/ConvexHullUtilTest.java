package com.vm325.inventory_back.imaging;

import georegression.struct.point.Point2D_F64;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConvexHullUtilTest {

    @Test
    void convexHull_cuadradoConPuntoInterior_excluyeElPuntoInterior() {
        List<Point2D_F64> points = List.of(
                new Point2D_F64(0, 0),
                new Point2D_F64(10, 0),
                new Point2D_F64(10, 10),
                new Point2D_F64(0, 10),
                new Point2D_F64(5, 5) // interior, no debe quedar en la envolvente
        );

        List<Point2D_F64> hull = ConvexHullUtil.convexHull(points);

        assertEquals(4, hull.size());
        assertEquals(100.0, ConvexHullUtil.area(hull), 1e-9);
    }

    @Test
    void area_cuadradoUnitarioEscalado_esCorrecta() {
        List<Point2D_F64> square = List.of(
                new Point2D_F64(0, 0),
                new Point2D_F64(20, 0),
                new Point2D_F64(20, 5),
                new Point2D_F64(0, 5)
        );

        assertEquals(100.0, ConvexHullUtil.area(square), 1e-9);
    }
}
