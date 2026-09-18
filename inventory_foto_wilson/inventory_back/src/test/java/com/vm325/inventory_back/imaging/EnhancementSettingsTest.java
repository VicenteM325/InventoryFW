package com.vm325.inventory_back.imaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnhancementSettingsTest {

    @Test
    void valoresDentroDeRango_noLanzan() {
        assertDoesNotThrow(() -> new EnhancementSettings(-100, 100, 0));
        assertDoesNotThrow(() -> new EnhancementSettings(100, -100, 100));
        assertDoesNotThrow(() -> new EnhancementSettings(0, 0, 0));
    }

    @Test
    void brightnessFueraDeRango_lanza() {
        assertThrows(IllegalArgumentException.class, () -> new EnhancementSettings(101, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new EnhancementSettings(-101, 0, 0));
    }

    @Test
    void contrastFueraDeRango_lanza() {
        assertThrows(IllegalArgumentException.class, () -> new EnhancementSettings(0, 101, 0));
        assertThrows(IllegalArgumentException.class, () -> new EnhancementSettings(0, -101, 0));
    }

    @Test
    void sharpnessFueraDeRango_lanza() {
        assertThrows(IllegalArgumentException.class, () -> new EnhancementSettings(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new EnhancementSettings(0, 0, 101));
    }

    @Test
    void default_esModoAutomatico() {
        assertEquals(0, EnhancementSettings.DEFAULT.brightness());
        assertEquals(0, EnhancementSettings.DEFAULT.contrast());
        assertEquals(15, EnhancementSettings.DEFAULT.sharpness());
    }
}
