package com.cg.base.widget.gather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.Face;
import android.util.AttributeSet;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

/**
 * @ProjectName: NVMS_SVN
 * @CreateDate: 2020/6/17 19:24
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FaceShowView extends View {



    private int previewWidth;
    private int previewHeight;



    private int cameraId;
    private Face[] faces;
    private float lineW;
    private Paint cPaint;

    float scale = 8f;//人脸举行框边长 和 框中某一直角长度 的比例关系


    public void setPreviewWH(Size cPixelSize) {
        this.previewWidth = cPixelSize.getWidth();
        this.previewHeight = cPixelSize.getHeight();
    }



    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }


    private ArrayList<RectF> list = new ArrayList<>();
    private Paint mPaint;

    public FaceShowView(Context context) {
        this(context,null);
    }

    public FaceShowView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FaceShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        lineW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getContext().getResources().getDisplayMetrics());
        mPaint.setStrokeWidth(lineW);
        mPaint.setAntiAlias(true);

        cPaint = new Paint();
        cPaint.setColor(Color.GREEN);
        cPaint.setStyle(Paint.Style.FILL);
        cPaint.setAntiAlias(true);
    }


    public void setFaces(Face[] faces) {
        this.faces = faces;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFaceRect(canvas);
    }


    /**
     * 绘制人脸框
     * @param canvas
     */
    private void drawFaceRect(Canvas canvas) {
        if (faces == null) {
            return;
        }
        for (int i = 0; i < faces.length; i++) {
            Rect fRect = faces[i].getBounds();
            //人脸检测坐标基于相机成像画面尺寸以及坐标原点。此处进行比例换算
            //成像画面与方框绘制画布长宽比比例（同画面角度情况下的长宽比例（此处前后摄像头成像画面相对预览画面倒置（±90°），计算比例时长宽互换））
            float scaleWidth = getHeight() * 1.0f / previewWidth;
            float scaleHeight = getWidth() * 1.0f / previewHeight;

            //坐标缩放
            int l = (int) (fRect.left * scaleWidth);
            int t = (int) (fRect.top * scaleHeight);
            int r = (int) (fRect.right * scaleWidth);
            int b = (int) (fRect.bottom * scaleHeight);
            //人脸检测坐标基于相机成像画面尺寸以及坐标原点。此处进行坐标转换以及原点(0,0)换算
            //人脸检测：坐标原点为相机成像画面的左上角，left、top、bottom、right以成像画面左上下右为基准
            //画面旋转后：原点位置不一样，根据相机成像画面的旋转角度需要换算到画布的左上角，left、top、bottom、right基准也与原先不一样，
            //如相对预览画面相机成像画面角度为90°那么成像画面坐标的top，在预览画面就为left。如果再翻转，那成像画面的top就为预览画面的right，且坐标起点为右，需要换算到左边

            if (cameraId == CameraCharacteristics.LENS_FACING_BACK) {
                //此处前置摄像头成像画面相对于预览画面顺时针90°+翻转。left、top、bottom、right变为bottom、right、top、left，并且由于坐标原点由左上角变为右下角，X,Y方向都要进行坐标换算
                int tx = canvas.getWidth() - b;
                int ty = canvas.getHeight() - r;
                int bx = canvas.getWidth() - t;
                int by = canvas.getHeight() - l;
                float linLen = (bx - tx) / scale;
                drawFaceRectPath(canvas, tx, ty, bx, by, linLen);
            } else {
                //此处后置摄像头成像画面相对于预览画面顺时针270°，left、top、bottom、right变为bottom、left、top、right，并且由于坐标原点由左上角变为左下角，Y方向需要进行坐标换算
                int tx = canvas.getWidth() - b;
                int ty = l;
                int bx = canvas.getWidth() - t;
                int by = r;
                float linLen = (by - ty) / scale;
                drawFaceRectPath(canvas, tx, ty, bx, by, linLen);
            }
        }
    }

    private void drawFaceRectPath(Canvas canvas, int tx, int ty, int bx, int by, float linLen) {
        Path path = new Path();
        path.moveTo(tx, ty);
        path.lineTo(linLen + tx, ty);
        path.moveTo(bx - linLen, ty);
        path.lineTo(bx, ty);

        path.moveTo(tx, by);
        path.lineTo(linLen + tx, by);
        path.moveTo(bx - linLen, by);
        path.lineTo(bx, by);

        path.moveTo(tx, ty);
        path.lineTo(tx, linLen + ty);
        path.moveTo(tx, by);
        path.lineTo(tx, by - linLen);

        path.moveTo(bx, ty);
        path.lineTo(bx, linLen + ty);
        path.moveTo(bx, by);
        path.lineTo(bx, by - linLen);

        canvas.drawPath(path, mPaint);

        canvas.drawCircle(tx, ty, lineW / 2, cPaint);
        canvas.drawCircle(bx, ty, lineW / 2, cPaint);
        canvas.drawCircle(tx, by, lineW / 2, cPaint);
        canvas.drawCircle(bx, by, lineW / 2, cPaint);
    }

}
