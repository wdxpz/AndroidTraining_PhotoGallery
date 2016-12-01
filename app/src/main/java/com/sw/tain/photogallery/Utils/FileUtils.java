package com.sw.tain.photogallery.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by home on 2016/12/1.
 */

public class FileUtils {
    //sd card root directory
    private static String mSdRootPath = Environment.getExternalStorageDirectory().getPath();

    //phone data root path
    private static String mDataRootPath = null;

    //image directory
    private final static String FOLDER_NAME = "/sw_images";

    public FileUtils(Context context){
        mDataRootPath = context.getCacheDir().getPath();
    }

    private String getStorageDirectory(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)?
                mSdRootPath+FOLDER_NAME : mDataRootPath+FOLDER_NAME;
    }

    public void saveBitmap(String fileName, Bitmap bitmap){
        if(bitmap==null) return;

        String path = getStorageDirectory();
        File folderFile = new File(path);
        if(!folderFile.exists()){
            folderFile.mkdir();
        }
        File file = new File(path+File.separator+fileName);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.d("FileUtils", "Failed to save image to local storage!");
            e.printStackTrace();
        }

    }

    public Bitmap getBitmap(String fileName){
        return BitmapFactory.decodeFile(getStorageDirectory()+File.separator+fileName);
    }

    public boolean isFileExists(String fileName){
        return new File(getStorageDirectory()+File.separator+fileName).exists();
    }

    public long getFileSize(String fileName){
        return new File(getStorageDirectory()+File.separator+fileName).length();
    }

    public void deleteFiles(){
        File dirFile = new File(getStorageDirectory());
        if(!dirFile.exists()){
            return;
        }

        if(dirFile.isDirectory()){
            String[] files = dirFile.list();
            for(String name : files){
                new File(dirFile, name).delete();
            }
        }

        dirFile.delete();
    }
}
