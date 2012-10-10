package com.rubika.aotalk.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageCache {
	private static final String APP_TAG = "ImageCache";
	
	public static Bitmap getImage(Context context, String path, String base, File cacheDirectory) {
		File cachedFile = null;
		
		if (cacheDirectory!= null) {
			cachedFile = new File(cacheDirectory.toString() + "/" + path);
		}
		
		if (cacheDirectory != null && cachedFile.exists()) {
			Logging.log(APP_TAG, "File exists, loading from cache");
			return BitmapFactory.decodeFile(cachedFile.toString());
		} else {
			Logging.log(APP_TAG, "File DONT exists, loading from web");
			Bitmap b = downloadImage(context, path, base);
			
			if (cacheDirectory != null && b != null) {
				File folder = new File(cachedFile.toString().replace(cachedFile.getName(), ""));
				if (!folder.exists()) {
					folder.mkdirs();
				}
				
				try {
					FileOutputStream out = new FileOutputStream(cachedFile);
					b.compress(Bitmap.CompressFormat.JPEG, 80, out);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return b;
		}
	}
	
	private static Bitmap downloadImage(Context context, String path, String base) {
		try {
			URL url = new URL(base + path);
									
			URLConnection connection = url.openConnection();
			connection.setUseCaches(true);
			
			return BitmapFactory.decodeStream((InputStream)connection.getContent());
		} catch (MalformedURLException e) {
			Logging.log(APP_TAG, e.getMessage());
			return null;
		} catch (IOException e) {
			Logging.log(APP_TAG, e.getMessage());
			return null;
		}
	}
	
	private static boolean isExternalStorageAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    private static boolean isExternalStorageReadOnly() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED_READ_ONLY);
    }
    
    public static File getCacheDirectory(String packageName, String subdirectoryName) {
        File cacheDirectory = null;
        
    	if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
            cacheDirectory = new File(
                    android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + "Android" 
                        + File.separator + "data" 
                        + File.separator + packageName
                        + File.separator + "cache" 
                        + File.separator + subdirectoryName
                        );
            if (!cacheDirectory.exists()) {
                if (cacheDirectory.mkdirs()) {
                    Logging.log(APP_TAG, "Cache directory '" + cacheDirectory + "' created");
                }
                else {
                    Logging.log(APP_TAG, "Cache directory '" + cacheDirectory + "' could not be created");
                    return null;
                }
            }
            else {
                Logging.log(APP_TAG, "Cache directory '" + cacheDirectory + "' is already present");
            }
            
            return cacheDirectory;
        } else {
        	return null;
        }
       
    }
}
