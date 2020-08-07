package com.cg.face.comparison.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.enums.RuntimeABI;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cg.base.utils.Constant;
import com.cg.face.comparison.contract.FaceCompareContract;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020-08-05 19:26:03
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FaceCompareModel extends FaceCompareContract.IModel {
    private FaceFeature mainFeature;
    /**
     * 选择图片时的类型
     */
    private static final int TYPE_SNAPSHOT = 0;
    private static final int TYPE_COMPARE = 1;

    public FaceCompareModel(){
    }


    /**
     * 图片处理
     * 检测人脸->特征值提取->人脸比对
     * @param faceEngine
     * @param bitmap
     * @param type
     * @param observer
     */
    public void processImage(FaceEngine faceEngine,Bitmap bitmap, int type, Observer<Integer>  observer) {
        if (bitmap == null) {
            if (type == TYPE_SNAPSHOT){
                observer.onError(new IllegalArgumentException("snapshot bitmap is null"));
            }else {
                observer.onError(new IllegalArgumentException("compare bitmap is null"));
            }
            return;
        }

        if (faceEngine == null) {
            observer.onError(new IllegalArgumentException("faceEngine is null"));
            return;
        }

        // 接口需要的bgr24宽度必须为4的倍数
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);

        if (bitmap == null) {
            if (type == TYPE_SNAPSHOT){
                observer.onError(new IllegalArgumentException("snapshot getAlignedBitmap null"));
            }else {
                observer.onError(new IllegalArgumentException("compare getAlignedBitmap null"));
            }
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // bitmap转bgr24
        long start = System.currentTimeMillis();
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            observer.onError(new IllegalArgumentException("failed to transform bitmap to imageData, code is " + transformCode));
            return;
        }
//        Log.i(TAG, "processImage:bitmapToBgr24 cost =  " + (System.currentTimeMillis() - start));

        List<FaceInfo> faceInfoList = new ArrayList<>();
        //人脸检测
        int detectCode = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
        if (detectCode != 0 || faceInfoList.size() == 0) {
            if (type == TYPE_COMPARE){
                mainFeature = null;
            }
            observer.onError(new IllegalArgumentException("face detection finished, code is " + detectCode + ", face num is " + faceInfoList.size() + " , type = " + type));
            return;
        }

        int faceProcessCode = faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);
        Log.i(TAG, "processImage: " + faceProcessCode);
        if (faceProcessCode != ErrorInfo.MOK) {
            if (type == TYPE_COMPARE){
                mainFeature = null;
            }
            observer.onError(new IllegalArgumentException("face process finished, code is " + faceProcessCode));
            return;
        }
        //年龄信息结果
        List<AgeInfo> ageInfoList = new ArrayList<>();
        //性别信息结果
        List<GenderInfo> genderInfoList = new ArrayList<>();
        //三维角度结果
        List<Face3DAngle> face3DAngleList = new ArrayList<>();
        //获取年龄、性别、三维角度
        int ageCode = faceEngine.getAge(ageInfoList);
        int genderCode = faceEngine.getGender(genderInfoList);
        int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);

        if ((ageCode | genderCode | face3DAngleCode) != ErrorInfo.MOK) {
            observer.onError(new IllegalArgumentException("at lease one of age、gender、face3DAngle detect failed! codes are: " + ageCode
                    + " ," + genderCode + " ," + face3DAngleCode));
            if (type == TYPE_COMPARE){
                mainFeature = null;
            }
            return;
        }

        //人脸比对数据显示
        if (faceInfoList.size() > 0) {
            if (type == TYPE_SNAPSHOT) {
                mainFeature = new FaceFeature();
                int res = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), mainFeature);
                if (res != ErrorInfo.MOK) {
                    mainFeature = null;
                }

            } else {
                FaceFeature faceFeature = new FaceFeature();
                int res = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), faceFeature);
                if (res == 0) {
                    FaceSimilar faceSimilar = new FaceSimilar();
                    int compareResult = faceEngine.compareFaceFeature(mainFeature, faceFeature, faceSimilar);
                    if (compareResult == ErrorInfo.MOK) {
                        observer.onNext((int)(faceSimilar.getScore() * 100));
                        observer.onComplete();
                    } else {
                        observer.onError(new IllegalArgumentException("compare failed compareResult = " + compareResult));
                    }
                }
                mainFeature = null;
            }
        }
    }


    /**
     * 人脸比对
     * 将path转换成bitmap->处理bitmap
     * @param context
     * @param faceEngine
     * @param snapshotPath
     * @param comparePath
     * @param observer
     */
    @Override
    public void compare(Context context,FaceEngine faceEngine, String snapshotPath, String comparePath, Observer<Integer> observer) {
        Glide.with(context).load(snapshotPath).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {

                Glide.with(context).load(comparePath).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(final Bitmap bitmap1, GlideAnimation<? super Bitmap> glideAnimation) {
                        progressImage(faceEngine,bitmap,bitmap1,observer);
                    }
                });
            }
        });


    }

    private void progressImage(FaceEngine faceEngine,Bitmap snapshotBitmap,Bitmap compareBitmap,Observer<Integer> observer){
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            processImage(faceEngine,snapshotBitmap,TYPE_SNAPSHOT,observer);
            Thread.sleep(500);
            processImage(faceEngine,compareBitmap,TYPE_COMPARE,observer);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(observer);
    }

    @Override
    public void activeEngine(Context context,Observer<Integer> observer) {
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
            Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);

            long start = System.currentTimeMillis();
            int activeCode = FaceEngine.activeOnline(context, Constant.APP_ID, Constant.SDK_KEY);
            Log.i(TAG, "subscribe cost: " + (System.currentTimeMillis() - start));
            emitter.onNext(activeCode);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}

