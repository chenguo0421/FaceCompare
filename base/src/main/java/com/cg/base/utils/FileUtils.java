package com.cg.base.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * @ProjectName: FaceCompare
 * @CreateDate: 2020/8/7 10:52
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FileUtils {

    /**
     * 获取网络缓存目录
     * @since V1.0
     */
    public static File getImageCacheDirPath(Context context) {
        File SDFolder;
        try {
            SDFolder = new File(getSDPath(context) ,File.separator + "data" + File.separator + "Image");
            if (!SDFolder.exists()) {
                boolean ret = SDFolder.mkdirs();
                if (ret) {
                    Log.i("CG","SDFolder.mkdir success");
                }
                ret = SDFolder.createNewFile();
                if (ret) {
                    Log.i("CG","SDFolder.createNewFile success");
                }
            }
            return SDFolder;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Android 10之后只允许在自己应用内创建目录
     * @param context
     * @return
     */
    public static String getSDPath(Context context) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            if (Build.VERSION.SDK_INT>=29){
                //Android10之后
                sdDir = context.getExternalFilesDir(null);
            }else {
                sdDir = Environment.getExternalStorageDirectory();// 获取SD卡根目录
            }
        } else {
            sdDir = Environment.getRootDirectory();// 获取跟目录
        }
        return sdDir.toString();
    }
}
