package com.rubika.aotalk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

public class ImageTools {
	private static final String APP_TAG = "--> The Leet ::ImageTools";
	
	public static Bitmap repleceIntervalColor(Bitmap bitmap,int redStart,int redEnd,int greenStart, int greenEnd,int blueStart, int blueEnd,int colorNew) {
	    if (bitmap != null) {
	        int picw = bitmap.getWidth();
	        int pich = bitmap.getHeight();
	        int[] pix = new int[picw * pich];
	        bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);
	        for (int y = 0; y < pich; y++) {
	            for (int x = 0; x < picw; x++) {
	                int index = y * picw + x;
	                    if (
	                        ((Color.red(pix[index]) >= redStart)&&(Color.red(pix[index]) <= redEnd))&&
	                        ((Color.green(pix[index]) >= greenStart)&&(Color.green(pix[index]) <= greenEnd))&&
	                        ((Color.blue(pix[index]) >= blueStart)&&(Color.blue(pix[index]) <= blueEnd))
	                    ){
	                        pix[index] = colorNew;
	                    }
	                }
	            }
	        Bitmap bm = Bitmap.createBitmap(pix, picw, pich,Bitmap.Config.ARGB_8888);
	        return bm;
	    }
	    return null;
	}
	
	public static Bitmap convertToBlackAndWhite(Bitmap sampleBitmap){
		ColorMatrix bwMatrix =new ColorMatrix();
		bwMatrix.setSaturation(0);
		
		final ColorMatrixColorFilter colorFilter= new ColorMatrixColorFilter(bwMatrix);
		
		Bitmap rBitmap = sampleBitmap.copy(Bitmap.Config.ARGB_8888, true);
		
		Paint paint = new Paint();
		paint.setColorFilter(colorFilter);
		
		Canvas myCanvas = new Canvas(rBitmap);
		myCanvas.drawBitmap(rBitmap, 0, 0, paint);
		
		return rBitmap;
	}
	
	public static Bitmap cropImage(Bitmap bitmap, Context context) {
		Logging.log(APP_TAG, "cropImage");
		
	    if (bitmap != null) {
	    	if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0 && bitmap.getHeight() > bitmap.getWidth()) {
		    	int startY = 0;
		    	
		    	if (bitmap.getHeight() - bitmap.getWidth()  > 1) {
		    		startY = Math.round((bitmap.getHeight() - bitmap.getWidth()) / 2);
		    	}
		    	
		    	if (startY + bitmap.getWidth() > bitmap.getHeight()) {
		    		startY = 0;
		    	}
		    	
		    	Logging.log(APP_TAG, "startY: " + startY + ", w: " + bitmap.getWidth() + ", h: " + bitmap.getHeight());
	    		
	    		bitmap = Bitmap.createBitmap(
		    			bitmap, 
		    			0, 
		    			startY, 
		    			bitmap.getWidth(),
		    			bitmap.getWidth()
		    		);
	    	}
	    	
	    	return bitmap;
	    } else {
	    	return null;
	    }
	}
	
	public static Bitmap resizeImage(Bitmap bitmap, int height, int width) {
		Logging.log(APP_TAG, "resizeImage");

		if (bitmap != null) {
	    	if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
				int currentWidth = bitmap.getWidth();
				int currentHeight = bitmap.getHeight();
		
				float scaleWidth = ((float) width) / currentWidth;
				float scaleHeight = ((float) height) / currentHeight;
				
				if (width > currentWidth || height > currentHeight) {
					scaleWidth = 1;
					scaleHeight = 1;
				}
		
				Matrix matrix = new Matrix();
				matrix.postScale(scaleWidth, scaleHeight);
		
				Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, currentWidth, currentHeight, matrix, false);
				return resizedBitmap;
	    	} else {
	    		return null;
	    	}
		} else {
			return null;
		}
	}
}
