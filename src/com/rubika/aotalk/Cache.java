/*
 * Cache.java
 *
 *************************************************************************
 * Copyright 2010 Christofer Engel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rubika.aotalk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class Cache {
	protected static final String APPTAG = "--> AOTalk::Cache";
	protected static final String sdpath  = "/AnarchyTalk/icons/";
	
	/**
	 * Get the icon for itemref, if its not in the cache it will be downloaded
	 * @param icon
	 * @param width
	 * @param height
	 * @return
	 */
	public Drawable getIcon(String icon, int width, int height) {
		if (icon.length() > 1) {
			String fileName = icon.substring(icon.lastIndexOf("/") + 1);
			
			File image = new File(Environment.getExternalStorageDirectory().toString() + sdpath + fileName);

			if(checkStorage()) {
				File folder = new File(Environment.getExternalStorageDirectory().toString() + sdpath);
				if(!folder.exists()) {
					folder.mkdirs();
					makeNomedia(folder.toString());
				}
				
				if(!image.exists()) {
					this.downloadImage(icon, sdpath);
		    	}
			}
			
			Drawable img = Drawable.createFromPath(image.toString());
			img.setBounds(0, 0, width, height);
			
			return img;
		} else {
			return null;
		}
    }
	
	
	/**
	 * Download image from web
	 * @param URL
	 * @param folder
	 * @return
	 */
	public boolean downloadImage(String URL, String folder) {        
	    Bitmap bitmap = null;
	    String filename = URL.substring(URL.lastIndexOf("/") + 1);
	       
	    boolean fetched = false;
		
	    InputStream in = null;
		
	    try {
	    	in = OpenHttpConnection(URL);
	    	if (in != null) {
	    		bitmap = BitmapFactory.decodeStream(in);
	    		in.close();
		    	fetched = true;
	    	}
	    } catch (IOException e1) {
	    	e1.printStackTrace();
	    }
	        
	    if (fetched && checkStorage()) {
	    	File imageDirectory = new File(Environment.getExternalStorageDirectory().toString() + folder);
	    	imageDirectory.mkdirs();
	    	
	    	OutputStream outStream = null;
		    File file = new File(Environment.getExternalStorageDirectory().toString() + folder, filename);
		    try {
				outStream = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
				outStream.flush();
				outStream.close();	    	   
		    } catch (FileNotFoundException e) {
				e.printStackTrace();
		    } catch (IOException e) {
				e.printStackTrace();
		    }
	    }

	    System.gc();
	    
	    return true;             
	}
	
	
	/**
	 * Open web stream
	 * @param urlString
	 * @return
	 * @throws IOException
	 */
	private InputStream OpenHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;
	               
		URL url = new URL(urlString); 
		URLConnection conn = url.openConnection();
	                 
		if (!(conn instanceof HttpURLConnection)) {
			Log.d("OpenHttpConnection", "Not an HTTP connection");
		}
		
		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			response = httpConn.getResponseCode();
			
			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		}
		catch (Exception ex) {
			Log.d("OpenHttpConnection", "Error connecting");
		}
		return in;     
	}
	
	
	/**
	 * Check if external storage is available
	 * @return
	 */
	public boolean checkStorage() {
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    return false;
		} else {
		    return false;
		}
	}
	
	/**
	 * Create .nomedia file in folder
	 * If it don't exist Android will show the content in albums
	 * @param path
	 * @return
	 */
	private boolean makeNomedia(String path) {
		boolean retval = false;
		
		File imageNomedia = new File(path + "/.nomedia");
    	if(!imageNomedia.exists()) {
    		try {
				imageNomedia.createNewFile();
				retval = true;
			} catch (IOException e) {
				Log.d(APPTAG, "Could not create .nomedia : " + e.getMessage());
			}
    	}
    	
    	return retval;
   	}
}
