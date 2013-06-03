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
	private static final String APP_TAG = "--> The Leet :: ImageCache";
	
	public static Bitmap getImage(Context context, String path, String base, File cacheDirectory, Bitmap.CompressFormat type) {
		File cachedFile = null;
		File hashedFile = null;
		
		String hashName = String.valueOf((base + path).hashCode());
		
		if (cacheDirectory!= null) {
			cachedFile = new File(cacheDirectory.toString() + "/" + path);
			hashedFile = new File(cachedFile.toString().replace(cachedFile.getName(), hashName));
		}
		
//		Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
//		Debug.getMemoryInfo(memoryInfo);
//		
//		if (Runtime.getRuntime().maxMemory() - (memoryInfo.getTotalPss() * 1024) > 43475 * 2) {
			if (hashedFile != null && cacheDirectory != null && hashedFile.exists()) {
				Logging.log(APP_TAG, "File '" + hashedFile.getName() + "' exist, loading from cache");
				return BitmapFactory.decodeFile(hashedFile.toString());
			} else {
				if (hashedFile != null && hashedFile.getName() != null) {
					Logging.log(APP_TAG, "File '" + hashedFile.getName() + "' DONT exist, loading from web");
				}
				
				Bitmap b = downloadImage(context, path, base);
				
				if (cacheDirectory != null && b != null) {
					File folder = new File(cachedFile.toString().replace(cachedFile.getName(), ""));
					if (!folder.exists()) {
						folder.mkdirs();
					}
					
					try {
						FileOutputStream out = new FileOutputStream(hashedFile.toString());
						b.compress(type, 100, out);
						
						Logging.log(APP_TAG, "File '" + path + "' saved as '" + hashedFile.toString() + "'");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
	
				return b;
			}
//		} else {
//			Logging.log(APP_TAG, "Low on memory!");
//			return null;
//		}
	}
	
	public static void preloadImage(Context context, String path, String base, File cacheDirectory, Bitmap.CompressFormat type) {
		File cachedFile = null;
		File hashedFile = null;
		
		String hashName = String.valueOf((base + path).hashCode());
		
		if (cacheDirectory!= null) {
			cachedFile = new File(cacheDirectory.toString() + "/" + path);
			hashedFile = new File(cachedFile.toString().replace(cachedFile.getName(), hashName));
		}
		
		if (cacheDirectory != null && hashedFile.exists()) {
			if (hashedFile != null && hashedFile.getName() != null) {
				Logging.log(APP_TAG, "File '" + hashedFile.getName() + "' exist, skipping");
			}
		} else {
			if (hashedFile != null && hashedFile.getName() != null) {
				Logging.log(APP_TAG, "File '" + hashedFile.getName() + "' DONT exist, loading from web");
			}
			
			Bitmap b = downloadImage(context, path, base);
			
			if (cacheDirectory != null && b != null) {
				File folder = new File(cachedFile.toString().replace(cachedFile.getName(), ""));
				if (!folder.exists()) {
					folder.mkdirs();
				}
				
				try {
					FileOutputStream out = new FileOutputStream(hashedFile.toString());
					b.compress(type, 100, out);
					
					Logging.log(APP_TAG, "File '" + path + "' saved as '" + hashedFile.toString() + "'");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static Bitmap downloadImage(Context context, String path, String base) {
		try {
			URL url = new URL(base + path);
			
			Logging.log(APP_TAG, url.toString());
									
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
    
    public static File getCacheDirectory(String packageName, String subdirectoryName) {
        File cacheDirectory = null;
        
    	if (StorageTools.isExternalStorageAvailable() && !StorageTools.isExternalStorageReadOnly()) {
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
            } else {
                Logging.log(APP_TAG, "Cache directory '" + cacheDirectory + "' exists");
            }
            
            return cacheDirectory;
        } else {
        	return null;
        }
    }
}
