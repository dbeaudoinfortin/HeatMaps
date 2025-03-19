package com.dbf.heatmaps.android.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

public class Utils {
	public static void removePartialTransparentPixels(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width];
        for (int y = 0; y < height; y++) {
            bitmap.getPixels(pixels, 0, width, 0, y, width, 1);
            for (int x = 0; x < width; x++) {
                int color = pixels[x];
                int alpha = Color.alpha(color);
                if (alpha > 0 && alpha < 255) {
                    if (alpha < 128) {
                        pixels[x] = Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
                    } else {
                        pixels[x] = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
                    }
                }
            }
            bitmap.setPixels(pixels, 0, width, 0, y, width, 1);
        }
    }
	
	public static int getFontSize(FontMetrics fontMetrics) {
		return (int) Math.ceil(Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent));
	}
	
	public static int measureText(Paint measurePaint, String text) {
		return (int) Math.ceil(measurePaint.measureText(text));
	}
}
