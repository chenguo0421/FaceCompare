package com.cg.face.album.presenter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.media.FaceDetector;
import android.media.Image;
import android.media.ImageReader;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.core.app.ActivityCompat;

import com.cg.base.base.BaseApplication;
import com.cg.base.utils.DataUtils;
import com.cg.base.utils.FastClickUtils;
import com.cg.base.utils.FileUtils;
import com.cg.face.album.contract.FaceGatherContract;
import com.cg.face.album.model.FaceGatherModel;


import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;


import io.reactivex.Observable;

import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * @ProjectName: NVMS_3.0
 * @CreateDate:  2020-07-16 11:39:30
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FaceGatherPresenter extends FaceGatherContract.IPresenter<FaceGatherContract.IView> {

    private final FaceGatherContract.IModel model;

    ReentrantLock closeLock = new ReentrantLock();

    private final String TAG = getClass().getName();

    private CameraManager cManager;
    private Surface previewSurface;//预览Surface
    private ImageReader cImageReader;
    private Surface captureSurface;//拍照Surface
    HandlerThread cHandlerThread;//相机处理线程
    Handler cHandler;//相机处理
    CameraDevice cDevice;
    CameraCaptureSession cSession;
    CameraDevice.StateCallback cDeviceOpenCallback = null;//相机开启回调
    CaptureRequest.Builder previewRequestBuilder;//预览请求构建
    CaptureRequest previewRequest;//预览请求
    CameraCaptureSession.CaptureCallback previewCallback;//预览回调
    CaptureRequest.Builder captureRequestBuilder;
    CaptureRequest captureRequest;
    CameraCaptureSession.CaptureCallback captureCallback;
    int[] faceDetectModes;
    Size cPixelSize;//相机成像尺寸
    int cOrientation;
    Size captureSize;
    boolean isFront;
    private boolean isClose = true;


    private Size size = new Size(480,640);


    private FaceDetector faceDetect;
    private int mCameraId = CameraCharacteristics.LENS_FACING_BACK;

    public FaceGatherPresenter(){
        model = new FaceGatherModel();
    }



    @Override
    public void startCamera() {
        closeCamera();
        if (isClose){
            if (getView().getTextureView().getTextureView().isAvailable()) {
                openCamera(mCameraId);
            } else {
                getView().getTextureView().getTextureView().setSurfaceTextureListener(null);
            }
        }
    }

    @Override
    public int switchCamera(){
        closeCamera();
        if (mCameraId == CameraCharacteristics.LENS_FACING_BACK){
            mCameraId = CameraCharacteristics.LENS_FACING_FRONT;
        }else {
            mCameraId = CameraCharacteristics.LENS_FACING_BACK;
        }
        if (isClose){
            if (getView().getTextureView().getTextureView().isAvailable()) {
                openCamera(mCameraId);
            } else {
                getView().getTextureView().getTextureView().setSurfaceTextureListener(null);
            }
        }
        return mCameraId;
    }

    @Override
    public void takePhoto() {
        if (FastClickUtils.isFastClick(1000)){
            return;
        }
        executeCapture();
    }

    @SuppressLint("NewApi")
    private void openCamera(int cameraId) {
        //前置摄像头
        String cId = cameraId + "";
        getView().getTextureView().getShowView().setCameraId(cameraId);

        cManager = (CameraManager) getView().getBaseActivity().getSystemService(Context.CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(getView().getBaseActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            showToast("请授予摄像头权限");
            ActivityCompat.requestPermissions(getView().getBaseActivity(), new String[]{Manifest.permission.CAMERA}, 0);
        } else {
            //根据摄像头ID，开启摄像头
            try {
                //获取开启相机的相关参数
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cId);
                cOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);//获取相机角度
                Rect cRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);//获取成像区域
                cPixelSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);//获取成像尺寸，同上

                Log.i(TAG, "获取相机角度 : " + cOrientation);
                getView().getTextureView().getShowView().setPreviewWH(cPixelSize);
                //可用于判断是否支持人脸检测，以及支持到哪种程度
                faceDetectModes = characteristics.get(CameraCharacteristics
                        .STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);//支持的人脸检测模式
                int maxFaceCount = characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
                //支持的最大检测人脸数量
                faceDetect = new FaceDetector(cPixelSize.getWidth(),cPixelSize.getHeight(),maxFaceCount);

                Size sSize = model.getMatchingSize(getView().getTextureView().getTextureView(),cId,cManager);
                //设置预览尺寸（避免控件尺寸与预览画面尺寸不一致时画面变形）
                getView().getTextureView().getTextureView().getSurfaceTexture().setDefaultBufferSize(sSize.getWidth(), sSize.getHeight());
                cManager.openCamera(cId, getCDeviceOpenCallback(), getCHandler());

            } catch (Exception e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
        }
    }



    /**
     * 初始化并获取相机开启回调对象。当准备就绪后，发起预览请求
     */
    @SuppressLint("NewApi")
    private CameraDevice.StateCallback getCDeviceOpenCallback() {
        if (cDeviceOpenCallback == null) {
            cDeviceOpenCallback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cDevice = camera;
                    try {
                        //创建Session，需先完成画面呈现目标（此处为预览和拍照Surface）的初始化
                        camera.createCaptureSession(Arrays.asList(getPreviewSurface(), getCaptureSurface()), new
                                CameraCaptureSession.StateCallback() {
                                    @Override
                                    public void onConfigured(@NonNull CameraCaptureSession session) {
                                        cSession = session;
                                        //构建预览请求，并发起请求
                                        Log.i(TAG, "[发出预览请求]");
                                        try {
                                            session.setRepeatingRequest(getPreviewRequest(), getPreviewCallback(),
                                                    getCHandler());
                                        } catch (CameraAccessException e) {
                                            Log.i(TAG, Log.getStackTraceString(e));
                                        }
                                    }

                                    @Override
                                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                        session.close();
                                    }
                                }, getCHandler());
                    } catch (CameraAccessException e) {
                        Log.i(TAG, Log.getStackTraceString(e));
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                }
            };
        }
        return cDeviceOpenCallback;
    }

    /**
     * 初始化并获取相机线程处理
     *
     * @return
     */
    private Handler getCHandler() {
        if (cHandler == null) {
            //单独开一个线程给相机使用
            cHandlerThread = new HandlerThread("cHandlerThread");
            cHandlerThread.start();
            cHandler = new Handler(cHandlerThread.getLooper());
        }
        return cHandler;
    }

    /**
     * 获取支持的最高人脸检测级别
     *
     * @return
     */
    private int getFaceDetectMode() {
        if (faceDetectModes == null) {
            return CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL;
        } else {
            return faceDetectModes[faceDetectModes.length - 1];
        }
    }

    /**
     * 初始化并获取预览回调对象
     *
     * @return
     */
    @SuppressLint("NewApi")
    private CameraCaptureSession.CaptureCallback getPreviewCallback() {
        if (previewCallback == null) {
            previewCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest
                        request, @NonNull TotalCaptureResult result) {
                    isClose = false;
                    onCameraImagePreviewed(result);
                }
            };
        }
        return previewCallback;
    }

    /**
     * 生成并获取预览请求
     *
     * @return
     */
    @SuppressLint("NewApi")
    private CaptureRequest getPreviewRequest() {
        previewRequest = getPreviewRequestBuilder().build();
        return previewRequest;
    }

    /**
     * 初始化并获取预览请求构建对象，进行通用配置，并每次获取时进行人脸检测级别配置
     *
     * @return
     */
    @SuppressLint("NewApi")
    private CaptureRequest.Builder getPreviewRequestBuilder() {
        if (previewRequestBuilder == null) {
            try {
                previewRequestBuilder = cSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(getPreviewSurface());
                previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);//自动曝光、白平衡、对焦
            } catch (CameraAccessException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
        }
        previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, getFaceDetectMode());//设置人脸检测级别
        previewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 0);
        return previewRequestBuilder;
    }

    /**
     * 获取预览Surface
     *
     * @return
     */
    private Surface getPreviewSurface() {
        if (previewSurface == null) {
            previewSurface = new Surface(getView().getTextureView().getTextureView().getSurfaceTexture());
        }
        return previewSurface;
    }

    /**
     * 处理相机画面处理完成事件，获取检测到的人脸坐标，换算并绘制方框
     *
     * @param result
     */
    @SuppressLint({"NewApi", "LocalSuppress"})
    private void onCameraImagePreviewed(CaptureResult result) {
        Face faces[] = result.get(CaptureResult.STATISTICS_FACES);
        Log.i(TAG, "检测到有人脸，进行拍照操作：faceLength=" + faces.length);
        if (faces.length > 0) {
            getView().getTextureView().getShowView().setFaces(faces);
        }else {
            getView().getTextureView().getShowView().setFaces(null);
        }
    }


    /**
     * 初始化拍照相关
     */
    @SuppressLint("NewApi")
    private Surface getCaptureSurface() {
        if (cImageReader == null) {
            cImageReader = ImageReader.newInstance(model.getCaptureSize(getView().getTextureView().getTextureView(),captureSize).getWidth(), model.getCaptureSize(getView().getTextureView().getTextureView(),captureSize).getHeight(),
                    ImageFormat.JPEG, 2);
            cImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    //拍照最终回调
                    onCaptureFinished(reader);
                }
            }, getCHandler());
            captureSurface = cImageReader.getSurface();
        }
        return captureSurface;
    }


    /**
     * 执行拍照
     */
    @SuppressLint("NewApi")
    private void executeCapture() {
        try {
            Log.i(TAG, "发出请求");
            cSession.capture(getCaptureRequest(), getCaptureCallback(), getCHandler());
        } catch (Exception e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
    }

    @SuppressLint("NewApi")
    private CaptureRequest getCaptureRequest() {
        captureRequest = getCaptureRequestBuilder().build();
        return captureRequest;
    }

    @SuppressLint("NewApi")
    private CaptureRequest.Builder getCaptureRequestBuilder() {
        if (captureRequestBuilder == null) {
            try {
                captureRequestBuilder = cSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                //设置拍照回调接口
                captureRequestBuilder.addTarget(getCaptureSurface());
                //TODO 1 照片旋转
                int rotation = getView().getBaseActivity().getWindowManager().getDefaultDisplay().getRotation();
                int rotationTo = model.getOrientation(rotation,cOrientation);
                captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotationTo);
            } catch (CameraAccessException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
        }
        return captureRequestBuilder;
    }

    @SuppressLint("NewApi")
    private CameraCaptureSession.CaptureCallback getCaptureCallback() {
        if (captureCallback == null) {
            captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest
                        request, @NonNull TotalCaptureResult result) {
                }
            };
        }
        return captureCallback;
    }

    /**
     * 处理相机拍照完成的数据
     *
     * @param reader
     */
    @SuppressLint("NewApi")
    private void onCaptureFinished(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        image.close();
        buffer.clear();
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        showImage(bitmap);
        Runtime.getRuntime().gc();
    }

    /**
     * 保存照片，调用人脸识别接口
     *
     * @param image
     */
    private void showImage(final Bitmap image) {
        reSizeBitmap(image,true);
    }

    public void showToast(String str) {
        getView().showToast(str);
    }

    @Override
    public synchronized void reSizeBitmap(Bitmap face,boolean needRevert) {
        if (face == null) {
            showToast("拍照出错，请重试！");
            return;
        }
        getView().getBaseActivity().showProgressDialog(60);
        Observable.create(emitter -> {
            try {
                final File file = new File(FileUtils.getImageCacheDirPath(BaseApplication.getInstance()), DataUtils.getCurrentDate("-") + "-" + DataUtils.getCurrentTime("-")+".jpg");
//            int minWidth = Math.min(face.getWidth(), face.getHeight());
//            int x = (face.getWidth() - minWidth) / 2;
//            int y = (face.getHeight() - minWidth) / 2;
//            Bitmap bitmap = Bitmap.createBitmap(face, x, y, minWidth, minWidth);
                model.resize(face, file, size.getWidth(), size.getHeight(),needRevert,mCameraId);
                getView().onCaptureFaceSuccess(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            getView().getBaseActivity().dismiss();
            emitter.onNext(new Object());
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe();
    }

    @Override
    public boolean isCloseCamera() {
        return isClose;
    }


    private void deleteFace(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }




    @SuppressLint("NewApi")
    @Override
    public void closeCamera() {
        closeLock.lock();
        synchronized (closeLock){
            if (cSession != null) {
                try {
                    cSession.stopRepeating();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                cSession.close();
                cSession = null;
            }

            if (cDevice != null) {
                cDevice.close();
                cDevice = null;
            }
            if (cImageReader != null) {
                cImageReader.close();
                cImageReader = null;
                captureRequestBuilder = null;
            }
            if (cHandlerThread != null) {
                cHandlerThread.quitSafely();
                try {
                    cHandlerThread.join();
                    cHandlerThread = null;
                    cHandler = null;
                } catch (InterruptedException e) {
                    Log.i(TAG, Log.getStackTraceString(e));
                }
            }

            if (captureRequestBuilder != null) {
                captureRequestBuilder.removeTarget(captureSurface);
                captureRequestBuilder = null;
            }
            if (captureSurface != null) {
                captureSurface.release();
                captureSurface = null;
            }
            if (previewRequestBuilder != null) {
                previewRequestBuilder.removeTarget(previewSurface);
                previewRequestBuilder = null;
            }
            if (previewSurface != null) {
                previewSurface.release();
                previewSurface = null;
            }
            isClose = true;
        }
        closeLock.unlock();
    }

}
