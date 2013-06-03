package com.rubika.aotalk.util;

import java.io.File;
import android.content.Context;

public class FileCache {
	private static final String APP_TAG = "--> The Leet :: FileCache";
    private File cacheDir;
    
    public FileCache(Context context){
    	if (StorageTools.isExternalStorageAvailable() && !StorageTools.isExternalStorageReadOnly()) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
            		+ File.separator + "Android" 
                    + File.separator + "data" 
                    + File.separator + context.getPackageName()
                    + File.separator + "cache"
                    + File.separator + "photos");
        } else {
            cacheDir = context.getCacheDir();
        }
        
        if(!cacheDir.exists()) {
            if (cacheDir.mkdirs()) {
                Logging.log(APP_TAG, "Cache directory '" + cacheDir + "' created");
            } else {
                Logging.log(APP_TAG, "Cache directory '" + cacheDir + "' could not be created");
            }
        }
    }
    
    public File getFile(String url){
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        
        return f;
    }
    
    public void clear(){
        File[] files = cacheDir.listFiles();
       
        if(files == null) {
            return;
        }
        
        for(File f : files) {
            f.delete();
        }
        
    	Logging.log(APP_TAG, "Deleted " + files.length + " files");
    }
}