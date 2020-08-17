package com.cg.face.album.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.cg.base.base.BaseActivity;
import com.cg.base.utils.Constant;
import com.cg.base.utils.DataUtils;
import com.cg.base.utils.FileUtils;
import com.cg.base.utils.ToastUtils;
import com.cg.base.widget.gather.FaceGatherTextureView;
import com.cg.face.R;
import com.cg.face.album.contract.FaceGatherContract;
import com.cg.face.album.intf.OnImageDialogBack;
import com.cg.face.album.presenter.FaceGatherPresenter;
import com.cg.face.album.view.fragment.ImageDialogFragment;

import java.io.File;


/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020-07-16 11:39:30
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FaceGatherActivity extends BaseActivity<FaceGatherContract.IView, FaceGatherContract.IPresenter<FaceGatherContract.IView>> implements FaceGatherContract.IView, FaceGatherTextureView.OnFaceGatherViewClick, View.OnClickListener, OnImageDialogBack {
    private static final int SELECT_PIC_BY_PICK_PHOTO = 1000;
    private static final int START_CAMERA = 200;
    private static final int CLOSE_CAMERA = 201;
    private static final int CROP_REQUEST_CODE = 202;
    private static String TAG = "DetectFaceActivity";
    private FaceGatherTextureView cView;//用于相机预览
    private AppCompatImageView iv_back;
    private int from;
    private File file;
    private Handler handler = new MyHandler();


    @Override
    protected void initView(View view) {
        cView = view.findViewById(R.id.surfaceView);
        iv_back = view.findViewById(R.id.iv_back);
    }

    @Override
    protected void initData() {
        from = getIntent().getIntExtra(Constant.FACE_GATHER_FROM,0);
    }

    @Override
    protected void initListener() {
        cView.setOnFaceGatherViewClick(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.face_activity_gather;
    }

    @Override
    protected FaceGatherContract.IPresenter<FaceGatherContract.IView> createPresenter() {
        return new FaceGatherPresenter();
    }

    @Override
    protected FaceGatherContract.IView createView() {
        return this;
    }

    @Override
    public BaseActivity getBaseActivity() {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(START_CAMERA,500);
    }


    @Override
    protected void onPause() {
        super.onPause();
        handler.sendEmptyMessage(CLOSE_CAMERA);
    }


    @Override
    public void onTakePhotoViewClick() {
        //拍照
        mPresenter.takePhoto();
    }

    @Override
    public void onSwitchCameraViewClick() {
        //前置/后置摄像头切换
        mPresenter.switchCamera();
    }

    @Override
    public void onOpenAlbumViewClick() {
        //打开相册
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);
        file = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PIC_BY_PICK_PHOTO && resultCode == RESULT_OK) {
            //拿到图片资源后开始图片裁剪
            startActivityForResult(crop(data.getData()), CROP_REQUEST_CODE);
        }else if (requestCode == CROP_REQUEST_CODE && resultCode == RESULT_OK){
            if (file != null) {
                onCaptureFaceSuccess(file.getAbsolutePath());
            }
        }
    }

    /**
     * 跳转系统裁剪图片
     * @param uri
     * @return
     */
    private Intent crop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");// 可裁剪
        intent.putExtra("aspectX", 1);// 裁剪的宽比例
        intent.putExtra("aspectY", 1);// 裁剪的高比例
        intent.putExtra("outputX", 800);// 裁剪的宽度
        intent.putExtra("outputY", 800);// 裁剪的高度
        intent.putExtra("scale", true);// 是否支持缩放

        file = new File(FileUtils.getImageCacheDirPath(this), DataUtils.getCurrentDate("-") + "-" + DataUtils.getCurrentTime("-") + ".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        // 是否返回数据
        intent.putExtra("return-data", false);
        // 裁剪成的图片的输出格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //是否关闭人脸识别
        //intent.putExtra("noFaceDetection", true);
        return intent;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back){
            handler.sendEmptyMessage(CLOSE_CAMERA);
            finish();
        }
    }

    @Override
    public FaceGatherTextureView getTextureView() {
        return cView;
    }

    @Override
    public void showToast(String message) {
        runOnUiThread(() -> ToastUtils.showToast(FaceGatherActivity.this,message));
    }


    @Override
    public void onCaptureFaceSuccess(final String path) {
        runOnUiThread(() -> {
            ImageDialogFragment.getInstance()
                    .load(path)
                    .from(from)
                    .width(cView.getWidth())
                    .height(cView.getHeight())
                    .callBack(FaceGatherActivity.this)
                    .show(getSupportFragmentManager(), "ImageDialogFragment");
            mPresenter.closeCamera();
        });
    }

    @Override
    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (!mPresenter.isCloseCamera()) {
            mPresenter.closeCamera();
        }
        super.onDestroy();
    }

    @Override
    public void onImageDialogBack() {
        mPresenter.startCamera();
    }

    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == START_CAMERA){
                mPresenter.startCamera();
            }else if (msg.what == CLOSE_CAMERA){
                mPresenter.closeCamera();
            }
        }
    }
}
