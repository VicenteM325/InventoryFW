package com.vm325.inventory_back.imaging;

/**
 * Resolución objetivo (en píxeles) a la que {@link ImageCropService} debe
 * recortar y reescalar una imagen. La relación de aspecto objetivo queda
 * implícita en targetWidthPx/targetHeightPx.
 */
public record CropSpec(int targetWidthPx, int targetHeightPx) {

    public double aspectRatio() {
        return (double) targetWidthPx / (double) targetHeightPx;
    }
}
