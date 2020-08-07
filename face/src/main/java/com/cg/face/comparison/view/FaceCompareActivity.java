package com.cg.face.comparison.view;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cg.base.base.BaseActivity;
import com.cg.base.utils.Constant;
import com.cg.base.utils.DensityUtils;
import com.cg.base.utils.FastClickUtils;
import com.cg.base.utils.ToastUtils;
import com.cg.base.widget.progress.InstrumentView;
import com.cg.face.R;
import com.cg.face.album.view.FaceGatherActivity;
import com.cg.face.comparison.contract.FaceCompareContract;
import com.cg.face.comparison.presenter.FaceComparePresenter;


/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020-08-05 19:26:03
 * @Author: ChenGuo
 * @Description: 人脸1V1
 * @Version: 1.0
 */
public class FaceCompareActivity extends BaseActivity<FaceCompareContract.IView, FaceCompareContract.IPresenter<FaceCompareContract.IView>> implements FaceCompareContract.IView, View.OnClickListener {

    private AppCompatImageView iv_snapshot;
    private AppCompatImageView iv_compare;
    private AppCompatTextView tv_compare;
    private AppCompatImageView iv_head_left;
    private AppCompatTextView tv_head_center;
    private int snapshotPicture = 1;
    private int comparePicture = 2;
    private InstrumentView iv_instrument;


    @Override
    protected void initView(View view) {
        iv_head_left = view.findViewById(R.id.iv_head_left);
        tv_head_center = view.findViewById(R.id.tv_head_center);
        iv_snapshot = view.findViewById(R.id.iv_snapshot);
        iv_compare = view.findViewById(R.id.iv_compare);
        tv_compare = view.findViewById(R.id.tv_compare);
        iv_instrument = view.findViewById(R.id.iv_instrument);
        iv_instrument.setSize(DensityUtils.dip2px(this,300));
    }

    @Override
    protected void initData() {
        iv_head_left.setImageResource(R.mipmap.icon_back);
        tv_head_center.setText("人脸比对");
        mPresenter.activeEngine();
    }

    @Override
    protected void initListener() {
        iv_head_left.setOnClickListener(this);
        iv_snapshot.setOnClickListener(this);
        iv_compare.setOnClickListener(this);
        tv_compare.setOnClickListener(this);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.face_activity_compare;
    }

    @Override
    protected FaceCompareContract.IPresenter<FaceCompareContract.IView> createPresenter() {
        return new FaceComparePresenter();
    }

    @Override
    protected FaceCompareContract.IView createView() {
        return this;
    }

    @Override
    public BaseActivity getBaseActivity() {
        return this;
    }

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 是否全部被允许
     */
    @Override
    public boolean checkPermission(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onCompareSuccess(Integer integer) {
        if (iv_instrument != null) {
            iv_instrument.setProgress(integer);
        }
    }

    @Override
    public void onCompareError(String message) {
        showToast(message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;
        for (int grantResult : grantResults) {
            isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        afterRequestPermission(requestCode, isAllGranted);
    }

    private void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == mPresenter.getPremissionCode()) {
            if (isAllGranted) {
                mPresenter.activeEngine();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (FastClickUtils.isFastClick(1000)){
            return;
        }
        if (v.getId() == R.id.iv_head_left){
            finish();
        }else if (v.getId() == R.id.tv_compare){
//            iv_instrument.setProgress(83);
            mPresenter.compare();
        }else if (v.getId() == R.id.iv_snapshot){
            mPresenter.setSnapshotPath(null);
            gotoFaceGatherActivity(snapshotPicture);
        }else if (v.getId() == R.id.iv_compare){
            mPresenter.setComparePath(null);
            gotoFaceGatherActivity(comparePicture);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2000 && data != null) {
            String path = null;
            if (resultCode == snapshotPicture) {//抓拍图
                path = data.getStringExtra(Constant.FACE_GATHER_PICTURE_PATH);
                mPresenter.setSnapshotPath(path);
                loadImage(path, iv_snapshot);
            } else if (resultCode == comparePicture) {//对比图
                path = data.getStringExtra(Constant.FACE_GATHER_PICTURE_PATH);
                mPresenter.setComparePath(path);
                loadImage(path, iv_compare);
            }

        }
    }

    /**
     * 展示人脸图片
     * @param path
     * @param iv
     */
    private void loadImage(String path, AppCompatImageView iv) {
        if (path != null && !path.isEmpty() && iv != null) {
            Glide.with(FaceCompareActivity.this).load(path).into(iv);
        }
    }

    /**
     * 跳转到人脸采集页面
     * @param from
     */
    private void gotoFaceGatherActivity(int from) {
        Intent intent = new Intent(this, FaceGatherActivity.class);
        intent.putExtra(Constant.FACE_GATHER_FROM,from);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent,2000);
    }

    @Override
    protected void onDestroy() {
        if (iv_instrument != null) {
            iv_instrument.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void showToast(String message) {
        runOnUiThread(() -> ToastUtils.showToast(FaceCompareActivity.this,message));
    }


}
