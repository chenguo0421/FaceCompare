package com.cg.face.album.view.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cg.base.base.BaseActivity;
import com.cg.base.base.BaseDialogFragment;
import com.cg.base.utils.Constant;
import com.cg.base.utils.ToastUtils;
import com.cg.face.R;
import com.cg.face.album.contract.ImageDialogFMContract;
import com.cg.face.album.intf.OnImageDialogBack;
import com.cg.face.album.presenter.ImageDialogFMPresenter;


/**
 * @ProjectName: NVMS_3.0
 * @CreateDate:  2020-07-16 11:11:09
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class ImageDialogFragment extends BaseDialogFragment<ImageDialogFMContract.IView, ImageDialogFMContract.IPresenter<ImageDialogFMContract.IView>> implements ImageDialogFMContract.IView, View.OnClickListener {

    private Bundle bundle;
    private String path;
    private int width;
    private int height;
    private AppCompatImageView iv_img;
    private FaceDetector faceDetect;
    private AppCompatImageView iv_back;
    private AppCompatImageView iv_sure;
    private OnImageDialogBack callBack;
    private int from;

    public ImageDialogFragment load(String path){
        this.path = path;
        return this;
    }

    public ImageDialogFragment width(int width){
        this.width = width;
        return this;
    }


    public ImageDialogFragment height(int height) {
        this.height = height;
        return this;
    }

    public ImageDialogFragment callBack(OnImageDialogBack callBack) {
        this.callBack = callBack;
        return this;
    }


    public ImageDialogFragment from(int from) {
        this.from = from;
        return this;
    }

    public static ImageDialogFragment getInstance(){
        return new ImageDialogFragment();
    }

    @Override
    protected ImageDialogFMContract.IPresenter<ImageDialogFMContract.IView> createPresenter() {
        return new ImageDialogFMPresenter();
    }

    @Override
    protected ImageDialogFMContract.IView createView() {
        return this;
    }

    @Override
    protected void initViews(View view) {
        iv_img = view.findViewById(R.id.iv_img);
        iv_back = view.findViewById(R.id.iv_back);
        iv_sure = view.findViewById(R.id.iv_sure);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.face_fragment_image;
    }

    @Override
    protected void initData() {
        if (path != null && !"".equals(path)) {
            Glide.with(getActivity()).load(path).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(final Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    faceDetect = new FaceDetector(bitmap.getWidth(),bitmap.getHeight(),1);
                    final int result = faceDetect.findFaces(bitmap,new FaceDetector.Face[1]);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_img.setImageBitmap(bitmap);
                            if (result == 1) {
                                //显示确认按钮
                                iv_sure.setVisibility(View.VISIBLE);
                            }else {
                                iv_sure.setVisibility(View.INVISIBLE);
                                ToastUtils.showToast(getActivity(),getString(R.string.face_str_warr_no_face));
                            }
                        }
                    });

                }
            });
        }
    }

    @Override
    protected void initListener() {
        iv_back.setOnClickListener(this);
        iv_sure.setOnClickListener(this);
    }

    @Override
    public void setBundleExtra(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    protected int fragmentIOAnimation() {
        return R.style.RightAnimation;
    }

    @Override
    protected int setDialogWidth() {
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected int setDialogHeight() {
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected float setOutSideAlpha() {
        return 1f;
    }

    @Override
    protected int setGravity() {
        return Gravity.CENTER;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back){
            if (callBack != null) {
                callBack.onImageDialogBack();
            }
            dismiss();
        }else if (v.getId() == R.id.iv_sure){
            if (getActivity() != null) {
                Intent intent = new Intent();
                intent.putExtra(Constant.FACE_GATHER_PICTURE_PATH,path);
                getActivity().setResult(from,intent);
                getActivity().finish();
                dismiss();
            }
        }
    }

}
