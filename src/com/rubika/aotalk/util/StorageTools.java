package com.rubika.aotalk.util;

public class StorageTools {
	//private static final String APP_TAG = "--> The Leet ::StorageTools";

	public static boolean isExternalStorageAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static boolean isExternalStorageReadOnly() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED_READ_ONLY);
    }

}
