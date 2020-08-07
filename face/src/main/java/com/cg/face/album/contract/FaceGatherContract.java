package com.cg.face.album.contract;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraManager;
import android.util.Size;
import android.view.TextureView;

import com.cg.base.base.mvp.BaseModel;
import com.cg.base.base.mvp.BasePresenter;
import com.cg.base.base.mvp.BaseView;
import com.cg.base.widget.gather.FaceGatherTextureView;


import java.io.File;

/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020-07-16 11:39:30
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public interface FaceGatherContract {
    interface IView extends BaseView {

        FaceGatherTextureView getTextureView();

        void showToast(String str);

        void onCaptureFaceSuccess(String absolutePath);
    }

    abstract class IPresenter<V extends BaseView> extends BasePresenter<V> {

        public abstract void closeCamera();

        public abstract void startCamera();

        public abstract void takePhoto();

        public abstract int switchCamera();

        public abstract void reSizeBitmap(Bitmap bitmap, boolean b);
    }

    abstract class IModel extends BaseModel {

        public abstract int getOrientation(int rotation, int cOrientation);

        public abstract Size getMatchingSize(TextureView textureView, String cId, CameraManager cManager);

        public abstract Size getCaptureSize(TextureView textureView, Size captureSize);

        public abstract void resize(Bitmap face, File file, int width, int height, boolean needRevert, int mCameraId);
    }
}
