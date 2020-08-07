package com.cg.face.album.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cg.base.base.BaseActivity;
import com.cg.base.utils.Constant;
import com.cg.base.utils.ToastUtils;
import com.cg.base.widget.gather.FaceGatherTextureView;
import com.cg.face.R;
import com.cg.face.album.contract.FaceGatherContract;
import com.cg.face.album.intf.OnImageDialogBack;
import com.cg.face.album.presenter.FaceGatherPresenter;
import com.cg.face.album.view.fragment.ImageDialogFragment;


/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020-07-16 11:39:30
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FaceGatherActivity extends BaseActivity<FaceGatherContract.IView, FaceGatherContract.IPresenter<FaceGatherContract.IView>> implements FaceGatherContract.IView, FaceGatherTextureView.OnFaceGatherViewClick, View.OnClickListener, OnImageDialogBack {

    private static String TAG = "DetectFaceActivity";
    private FaceGatherTextureView cView;//用于相机预览
    private AppCompatImageView iv_back;
    private int from;


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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.startCamera();
            }
        },300);
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
        Intent intent = new Intent(Intent.ACTION_PICK);  //跳转到 ACTION_IMAGE_CAPTURE
        intent.setType("image/*");
        startActivityForResult(intent,1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                Glide.with(FaceGatherActivity.this).load(uri).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        mPresenter.reSizeBitmap(bitmap, false);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back){
            mPresenter.closeCamera();
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
        mPresenter.closeCamera();
        super.onDestroy();
    }

    @Override
    public void onImageDialogBack() {
        mPresenter.startCamera();
    }
}
