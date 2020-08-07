package com.cg.base.widget.gather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.cg.base.R;


/**
 * @ProjectName: NVMS_SVN
 * @CreateDate: 2020/6/17 14:51
 * @Author: ChenGuo
 * @Description: 人脸采集视图控件 预览画面采用 3：4 的比例，输出图片为480 * 640的 3：4图片
 * @Version: 1.0
 */
public class FaceGatherTextureView extends FrameLayout implements View.OnClickListener {

    private TextureView textureView;
    private FaceShowView showView;
    private AppCompatImageView takePhoto;
    private AppCompatImageView switchCamera;
    private OnFaceGatherViewClick listener;
    private AppCompatImageView openAlbum;

    public FaceGatherTextureView(Context context) {
        this(context,null);
    }

    public FaceGatherTextureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FaceGatherTextureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("RtlHardcoded")
    private void init() {
        textureView = new TextureView(getContext());
        Point point = getScreenPoint(getContext());
        int width = point.x;
        int height = point.x * 4 / 3;
        LayoutParams params = new LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        addView(textureView,params);

        showView = new FaceShowView(getContext());
        LayoutParams params1 = new LayoutParams(width, height);
        params1.gravity = Gravity.CENTER;
        addView(showView,params1);

        takePhoto = new AppCompatImageView(getContext());
        takePhoto.setImageResource(R.drawable.common_bg_btn_takephoto);
        LayoutParams params2 = new LayoutParams(width / 6, width / 6);
        params2.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params2.bottomMargin = width / 10;
        addView(takePhoto,params2);
        takePhoto.setOnClickListener(this);

        switchCamera = new AppCompatImageView(getContext());
        switchCamera.setImageResource(R.mipmap.icon_switchcamera);
        LayoutParams params3 = new LayoutParams(width / 10, width / 10);
        params3.gravity = Gravity.BOTTOM | Gravity.LEFT;
        int bottomMargin = width / 10 + (width / 6 - width / 10) / 2;
        params3.bottomMargin = bottomMargin;
        params3.leftMargin = width / 10;
        addView(switchCamera,params3);
        switchCamera.setOnClickListener(this);

        openAlbum = new AppCompatImageView(getContext());
        openAlbum.setImageResource(R.mipmap.icon_album);
        LayoutParams params4 = new LayoutParams(width / 10, width / 10);
        params4.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params4.bottomMargin = bottomMargin;
        params4.rightMargin = width / 10;
        addView(openAlbum,params4);
        openAlbum.setOnClickListener(this);

    }

    public TextureView getTextureView() {
        return textureView;
    }

    public FaceShowView getShowView() {
        return showView;
    }


    public static Point getScreenPoint(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(outMetrics);
            int widthPixels = outMetrics.widthPixels;
            int heightPixels = outMetrics.heightPixels;
            int densityDpi = outMetrics.densityDpi;
            float density = outMetrics.density;
            float scaledDensity = outMetrics.scaledDensity;
            //可用显示大小的绝对宽度（以像素为单位）。
            //可用显示大小的绝对高度（以像素为单位）。
            //屏幕密度表示为每英寸点数。
            //显示器的逻辑密度。
            //显示屏上显示的字体缩放系数。
            Log.d("display", "widthPixels = " + widthPixels + ",heightPixels = " + heightPixels + "\n" +
                    ",densityDpi = " + densityDpi + "\n" +
                    ",density = " + density + ",scaledDensity = " + scaledDensity);
            return new Point(widthPixels,heightPixels);
        }
        return new Point();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            if (v == takePhoto){
                listener.onTakePhotoViewClick();
            }else if (v == switchCamera){
                listener.onSwitchCameraViewClick();
            }else if (v == openAlbum){
                listener.onOpenAlbumViewClick();
            }
        }

    }

    public void setOnFaceGatherViewClick(OnFaceGatherViewClick listener){
        this.listener = listener;
    }

    public interface OnFaceGatherViewClick{
        void onTakePhotoViewClick();
        void onSwitchCameraViewClick();
        void onOpenAlbumViewClick();
    }
}
