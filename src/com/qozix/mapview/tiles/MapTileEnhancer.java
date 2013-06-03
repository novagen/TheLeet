package com.qozix.mapview.tiles;

import android.graphics.Canvas;

/**
 *
 */
public interface MapTileEnhancer {

    public void drawOn(Canvas canvas, int zoom, int row, int column);
}
