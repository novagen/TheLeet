package com.qozix.mapview.tiles;

import com.rubika.aotalk.util.Logging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

public class MapTile {
	private static final BitmapFactory.Options OPTIONS = new BitmapFactory.Options();
	static {
		OPTIONS.inPreferredConfig = Bitmap.Config.RGB_565;
	}

	private int zoom;

	private int row;
	private int column;
	private int left;
	private int top;
	private int width;
	private int height;
	private int right;
	private int bottom;

	private String pattern;

	private ImageView imageView;
	private Bitmap bitmap;

	private boolean hasBitmap;

	public MapTile() {

	}

	public MapTile( int z, int r, int c, int w, int h, String p ) {
		set( z, r, c, w, h, p );
	}

	public void set( int z, int r, int c, int w, int h, String p ) {
		zoom = z;
		row = r;
		column = c;
		width = w;
		height = h;
		top = r * h;
		left = c * w;
		right = left + w;
		bottom = top + h;
		pattern = p;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	public int getLeft() {
		return left;
	}

	public int getTop() {
		return top;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getBottom() {
		return bottom;
	}

	public int getRight() {
		return right;
	}

	public int getZoom() {
		return zoom;
	}

	public ImageView getImageView() {
		return imageView;
	}

	public String getFileName() {
		return pattern.replace( "%col%", Integer.toString( column ) ).replace( "%row%", Integer.toString( row ) );
	}

	public void decode( Context context, MapTileCache cache, MapTileDecoder decoder, MapTileEnhancer enhancer ) {
		if ( hasBitmap ) {
			return;
		}
		String fileName = getFileName();
		if ( cache != null ) {
			Logging.log("MapTile", "Cache exists");
			Bitmap cached = cache.getBitmap( fileName );
			if ( cached != null ) {
				bitmap = cached;
                enhanceBitmap(enhancer);
				return;
			}	
		} else {
			Logging.log("MapTile", "Cache is null");
		}
		bitmap = decoder.decode( fileName, context );
		hasBitmap = ( bitmap != null );
		if ( cache != null && hasBitmap ) {
			cache.addBitmap( fileName, bitmap );
		}

        enhanceBitmap(enhancer);
    }

    private void enhanceBitmap(MapTileEnhancer enhancer) {
        if (enhancer != null && bitmap != null) {
            Bitmap enhancedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(enhancedBitmap);
            if(bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0, null);
                enhancer.drawOn(canvas, getZoom(), getRow(), getColumn());
                bitmap = enhancedBitmap;
            }
        }
    }

	public boolean render( Context context ) {
		if ( imageView == null ) {
			imageView = new ImageView( context );
			imageView.setAdjustViewBounds( false );
			imageView.setScaleType( ImageView.ScaleType.MATRIX );
		}
		imageView.setImageBitmap( bitmap );
        bitmap = null;
		return true;
	}

	public void destroy() {
		if ( imageView != null ) {
			imageView.setImageBitmap( null );
			ViewParent parent = imageView.getParent();
			if ( parent != null && parent instanceof ViewGroup ) {
				ViewGroup group = (ViewGroup) parent;
				group.removeView( imageView );
			}
			imageView = null;
		}
		hasBitmap = false;
		bitmap = null;
	}

	@Override
	public boolean equals( Object o ) {
		if ( o instanceof MapTile ) {
			MapTile m = (MapTile) o;
			return ( m.getRow() == getRow() )
				&& ( m.getColumn() == getColumn() )
				&& ( m.getZoom() == getZoom() );
		}
		return false;
	}

	@Override
	public String toString() {
		return "(left=" + left + ", top=" + top + ", right=" + right + ", bottom=" + bottom + ")";
	}

}
