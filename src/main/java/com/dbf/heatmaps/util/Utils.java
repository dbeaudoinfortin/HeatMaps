package com.dbf.heatmaps.util;

import java.awt.image.BufferedImage;

public class Utils {
	public static void removePartialTransparentPixels(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int argb = image.getRGB(x, y);
                final int alpha = (argb >> 24) & 0xFF;
                if(alpha > 0 && alpha < 255) {
                	 if (alpha < 128) {
                         //Make fully transparent
                         image.setRGB(x, y, 0);
                     } else {
                    	 //Make fully opaque
                    	 image.setRGB(x, y, (255 << 24) | (argb & 0x00FFFFFF));
                     }
                }
            }
        }
    }
}
