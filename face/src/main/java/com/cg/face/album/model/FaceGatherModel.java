package com.cg.face.album.model;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.RequiresApi;


import com.cg.face.album.contract.FaceGatherContract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020-07-16 11:39:30
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FaceGatherModel extends FaceGatherContract.IModel {

    //为了使照片竖直显示
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public FaceGatherModel(){}


    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    @Override
    public int getOrientation(int rotation,int cOrientation) {
        return (ORIENTATIONS.get(rotation) + cOrientation + 270) % 360;
    }


    /**
     * 获取匹配的大小 这里是Camera2获取分辨率数组的方式,Camera1获取不同,计算一样
     * @return
     */
    @Override
    public Size getMatchingSize(TextureView textureView, String mCameraId, CameraManager cManager){
        Size selectSize = null;
        float selectProportion = 0;
        try {
            float viewProportion = (float)textureView.getWidth() / (float)textureView.getHeight();//计算View的宽高比
            CameraCharacteristics cameraCharacteristics = cManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
            Size[] sizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);//获取预览尺寸

//            for (int i = 0; i < previewSizes.length; i++) {
//                Size itemSize = previewSizes[i];
//                Log.e(TAG, "getMatchingSize: previewSizes支持的尺寸="+itemSize.getWidth()+"高度="+itemSize.getHeight());
//            }

            for (int i = 0; i < sizes.length; i++){
                Size itemSize = sizes[i];
                Log.e(TAG, "getMatchingSize: 支持的尺寸="+itemSize.getWidth()+"高度="+itemSize.getHeight());
                float itemSizeProportion = (float)itemSize.getHeight() / (float)itemSize.getWidth();//计算当前分辨率的高宽比
                float differenceProportion = Math.abs(viewProportion - itemSizeProportion);//求绝对值
                Log.e(TAG, "相减差值比例="+differenceProportion );
                if (i == 0){
                    selectSize = itemSize;
                    selectProportion = differenceProportion;
                    continue;
                }
                if (differenceProportion <= selectProportion){ //判断差值是不是比之前的选择的差值更小
                    if (differenceProportion == selectProportion){ //如果差值与之前选择的差值一样
                        if (selectSize.getWidth() + selectSize.getHeight() < itemSize.getWidth() + itemSize.getHeight()){//选择分辨率更大的Size
                            selectSize = itemSize;
                            selectProportion = differenceProportion;
                        }

                    }else {
                        selectSize = itemSize;
                        selectProportion = differenceProportion;
                    }
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "getMatchingSize: 选择的比例是="+selectProportion);
        Log.e(TAG, "getMatchingSize: 选择的尺寸是 宽度="+selectSize.getWidth()+"高度="+selectSize.getHeight());
//        selectSize = new Size(1080,1920);
//        textureView.getShowView().setPreviewWH(selectSize);
        return selectSize;
    }


    /**
     * 获取拍照尺寸
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Size getCaptureSize(TextureView textureView,Size captureSize) {
        if (captureSize != null) {
            return captureSize;
        } else {
            return new Size(textureView.getWidth(), textureView.getHeight());
        }
    }


    @Override
    public void resize(Bitmap bitmap, File outputFile, int maxWidth, int maxHeight, boolean needRevert, int mCameraId) {
        try {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            // 图片大于最大高宽，按大的值缩放
            if (bitmapWidth > maxHeight || bitmapHeight > maxWidth) {
                float widthScale = maxWidth * 1.0f / bitmapWidth;
                float heightScale = maxHeight * 1.0f / bitmapHeight;
                Log.d("CGTEST","bitmapWidth = " + bitmapWidth + " , bitmapHeight = " + bitmapHeight + " , maxWidth = " + maxWidth + " , maxHeight = " + maxHeight);
                float scale = Math.max(widthScale, heightScale);
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
                if (needRevert && mCameraId == CameraCharacteristics.LENS_FACING_BACK){
                    bitmap = convertBitmap(bitmap);//矫正，避免前置摄像头镜像图片
                }
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap convertBitmap(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(-1,1);
        return Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
    }

}

