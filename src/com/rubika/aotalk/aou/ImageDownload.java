package com.rubika.aotalk.aou;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import com.rubika.aotalk.util.Logging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;

public class ImageDownload implements ImageGetter {
	private static final String APP_TAG = "--> The Leet :: ImageDownload";

	private long maximumAge = 86400000;

	@Override
	public Drawable getDrawable(String source) {                  
		String sdState = android.os.Environment.getExternalStorageState();
		Context context = AOU.getAppContext();

		File cacheDir;

		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdDir = context.getExternalCacheDir();
			cacheDir = new File(sdDir, "images");
		} else {
			cacheDir = context.getCacheDir();
		}

		if(!cacheDir.exists()) {
			cacheDir.mkdirs();

			File nomedia = new File(cacheDir, ".nomedia");

			if(!nomedia.exists()) {
				try {
					nomedia.createNewFile();
				} catch (IOException e) {
					Logging.log(APP_TAG, e.getMessage());
				}
			}
		}


		if (!source.startsWith("http")) {
			source = "http://www.ao-universe.com/" + source;
		}

		String url = source.replace(" ", "%20");
		String filename = String.valueOf(url.hashCode()).replace("-", "") + url.substring(url.lastIndexOf("."));
		String filetype = url.substring(url.lastIndexOf(".") + 1);
		File f = new File(cacheDir, filename);

		Date date = new Date();

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inTempStorage = new byte[8*1024];

		Drawable d;

		if (f.exists()) {
			if (maximumAge > 0 && (date.getTime() - f.lastModified()) > maximumAge) {
				d = downloadImage(url, filetype, cacheDir, context);				
			} else {
				Logging.log(APP_TAG, "Using local file");
				d = (Drawable) new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(f.getPath(), options));
			}
		} else {
			d = downloadImage(url, "jpg", cacheDir, context);
		}

		return d;
	}

	private Drawable downloadImage(String url, String type, File cacheDir, Context context) {
		Logging.log(APP_TAG, "Using remote file");
		String filename = String.valueOf(url.hashCode()).replace("-", "") + "." + type;
		File f = new File(cacheDir, filename);
		Bitmap bitmap = null;

		try {
			bitmap = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
			writeFile(bitmap, f, type);

			return (Drawable) new BitmapDrawable(context.getResources(), bitmap);
		} catch (Exception e) {
			Logging.log(APP_TAG, e.getMessage());
			return null;
		}
	}

	private void writeFile(Bitmap bmp, File f, String type) {
		FileOutputStream out = null;
		CompressFormat format = null;
		
		if (type.equals("jpg")) {
			format = Bitmap.CompressFormat.JPEG;
		} else if (type.equals("png")) {
			format = Bitmap.CompressFormat.PNG;
		}

		try {
			out = new FileOutputStream(f);
			bmp.compress(format, 80, out);
		} catch (Exception e) {
			Logging.log(APP_TAG, e.getMessage());
		}
		finally { 
			try { 
				if (out != null ) out.close(); 
			} catch(Exception e) {
				Logging.log(APP_TAG, e.getMessage());
			} 
		}
	}
}
