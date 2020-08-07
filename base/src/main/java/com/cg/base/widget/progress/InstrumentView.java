package com.cg.base.widget.progress;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cg.base.utils.DensityUtils;


/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020/8/6 10:03
 * @Author: ChenGuo
 * @Description: 半圆环仪表盘进度条，共计180°，总计百分比100%， 从0-100分51小块，0°为第0块，正90°为第25块，正180°为第50块
 * @Version: 1.0
 */
public class InstrumentView extends View {

    private int width;//控件宽度
    private int height;//控件高度
    private int similarityAreaHeight;//相似度文字区域高度

    private int centerX ;//圆心X
    private int centerY;//圆心Y
    private int centerColor = Color.parseColor("#696969");
    private int blueColor = Color.parseColor("#00a1e4");
    private int orangeColor = Color.parseColor("#FFA500");
    private int redColor = Color.parseColor("#FF0000");


    private float anger = 0;//起始角度
    private int strokeWidth = 8;//刻度线的宽度
    private int outStrokeWidth = 16;//刻度线的宽度

    private RectF innerRectF;

    private Paint centerPointPaint;//黑色圆心画笔
    private Paint linePaint;//内半圆的线的画笔
    private Paint outLineBluePaint;//内半圆的线的画笔
    private Paint outLineOrangePaint;//内半圆的线的画笔
    private Paint outLineRedPaint;//内半圆的线的画笔
    private Paint textPaint;//文字画笔
    private Paint scaleTextPaint;//刻度画笔

    private int innerRadio;//内圆半径（刻度圆弧）
    private int progress;
    private int startProgress = 0;
    private int handlerDelayTime = 30;
    private MyHandler handler = new MyHandler();
    private String text;
    private float[] arr;


    public InstrumentView(Context context) {
        this(context,null);
    }

    public InstrumentView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public InstrumentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        centerPointPaint = new Paint();
        centerPointPaint.setAntiAlias(true);
        centerPointPaint.setStyle(Paint.Style.FILL);
        centerPointPaint.setColor(centerColor);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(strokeWidth);
        linePaint.setColor(centerColor);


        outLineRedPaint = new Paint();
        outLineRedPaint.setAntiAlias(true);
        outLineRedPaint.setStyle(Paint.Style.STROKE);
        outLineRedPaint.setStrokeWidth(outStrokeWidth);
        outLineRedPaint.setColor(redColor);

        outLineOrangePaint = new Paint();
        outLineOrangePaint.setAntiAlias(true);
        outLineOrangePaint.setStyle(Paint.Style.STROKE);
        outLineOrangePaint.setStrokeWidth(outStrokeWidth);
        outLineOrangePaint.setColor(orangeColor);

        outLineBluePaint = new Paint();
        outLineBluePaint.setAntiAlias(true);
        outLineBluePaint.setStyle(Paint.Style.STROKE);
        outLineBluePaint.setStrokeWidth(outStrokeWidth);
        outLineBluePaint.setColor(blueColor);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(blueColor);

        scaleTextPaint = new Paint();
        scaleTextPaint.setAntiAlias(true);
        scaleTextPaint.setStyle(Paint.Style.FILL);
        scaleTextPaint.setTextAlign(Paint.Align.CENTER);
        scaleTextPaint.setColor(centerColor);
    }


    public void setProgress(int progress){
        startProgress = 0;
        if (progress <= 0) {
            this.progress = 0;
        }
        if (progress >= 100) {
            this.progress = 100;
        }
        this.progress = progress;
        if (this.progress > 0) {
            handler.sendEmptyMessageDelayed(startProgress,handlerDelayTime);
        }
    }



    /**
     * 设置控件大小
     * @param width
     */
    public void setSize(int width){
        this.width = width;
        similarityAreaHeight = DensityUtils.dip2px(getContext(), 30);
        this.height = width / 2 + similarityAreaHeight;
        centerX = width/2;
        centerY = height - similarityAreaHeight;

        if (textPaint == null) {
            initPaint();
        }
        textPaint.setTextSize(width / 15);
        scaleTextPaint.setTextSize(width / 30);
        text = "相似度：" + startProgress + " %";
        arr = new float[text.length()];

        innerRectF = new RectF(width / 10, width / 10, 9 * width / 10, 9 * width / 10);
        innerRadio = 2 * width / 5;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (innerRectF == null) {
            return;
        }
        //绘制圆弧
        canvas.drawArc(innerRectF,180,180,false,linePaint);
        //绘制中心点
        canvas.drawCircle(centerX,centerY,20,centerPointPaint);

        //绘制刻度
        for (int i = 180; i <= 360; i+=9) {
            double degrees = Math.toRadians(i - 180);
            if ((i - 180) % 45 == 0) {//绘制大刻度
                if (i == 180){
                    canvas.drawLine((float) (centerX - (innerRadio * Math.cos(degrees)) - strokeWidth / 2),(float)(centerY - (innerRadio * Math.sin(degrees))),(float) (centerX - ( (7 * innerRadio / 8) * Math.cos(degrees))),(float) (centerY - ((7 * innerRadio / 8) * Math.sin(degrees))),linePaint);
                    canvas.drawText("0",(float) (centerX - ( (13 * innerRadio / 16) * Math.cos(degrees))),centerY,scaleTextPaint);
                } else if (i == 360){
                    canvas.drawLine((float) (centerX - (innerRadio * Math.cos(degrees)) + strokeWidth / 2),(float)(centerY - (innerRadio * Math.sin(degrees))),(float) (centerX - ( (7 * innerRadio / 8) * Math.cos(degrees))),(float) (centerY - ((7 * innerRadio / 8) * Math.sin(degrees))),linePaint);
                    canvas.drawText("100",(float) (centerX - ( (12 * innerRadio / 16) * Math.cos(degrees))),centerY,scaleTextPaint);
                }else {
                    canvas.drawLine((float) (centerX - (innerRadio * Math.cos(degrees))),(float)(centerY - (innerRadio * Math.sin(degrees))),(float) (centerX - ( (7 * innerRadio / 8) * Math.cos(degrees))),(float) (centerY - ((7 * innerRadio / 8) * Math.sin(degrees))),linePaint);
                    if (i == 270){
                        canvas.drawText("50",(float) centerX,(float) (centerY - ((12 * innerRadio / 16) * Math.sin(degrees))),scaleTextPaint);
                    }
                }
            } else if ((i - 180) % 9 == 0) {//绘制小刻度
                canvas.drawLine((float) (centerX - (innerRadio * Math.cos(degrees))),(float)(centerY - (innerRadio * Math.sin(degrees))),(float) (centerX - ( (19 * innerRadio / 20) * Math.cos(degrees))),(float) (centerY - ((19 * innerRadio / 20) * Math.sin(degrees))),linePaint);
            }
        }

        //绘制外层包裹着的线
        for (int i = 180; i <= 360; i+=5) {
            double degrees = Math.toRadians(i - 180);
            if ((i-180) % 5 == 0){
                if (i < 240) {
                    canvas.drawLine((float) (centerX - ((innerRadio + width / 30) * Math.cos(degrees))),(float)(centerY - ((innerRadio + width / 30) * Math.sin(degrees))),(float) (centerX - ((innerRadio + width / 15) * Math.cos(degrees))),(float) (centerY - ((innerRadio + width / 15) * Math.sin(degrees))),outLineBluePaint);
                } else if (i < 300) {
                    canvas.drawLine((float) (centerX - ((innerRadio + width / 30) * Math.cos(degrees))),(float)(centerY - ((innerRadio + width / 30) * Math.sin(degrees))),(float) (centerX - ((innerRadio + width / 15) * Math.cos(degrees))),(float) (centerY - ((innerRadio + width / 15) * Math.sin(degrees))),outLineOrangePaint);
                }else {
                    canvas.drawLine((float) (centerX - ((innerRadio + width / 30) * Math.cos(degrees))),(float)(centerY - ((innerRadio + width / 30) * Math.sin(degrees))),(float) (centerX - ((innerRadio + width / 15) * Math.cos(degrees))),(float) (centerY - ((innerRadio + width / 15) * Math.sin(degrees))),outLineRedPaint);
                }
            }
        }

        //画指针
        double degrees = Math.toRadians((double) startProgress * 180 / 100);
        canvas.drawLine((float) (centerX - ((innerRadio * 13 / 16 ) * Math.cos(degrees))),(float)(centerY - ((innerRadio * 13 / 16 ) * Math.sin(degrees))),(float) (centerX + ((similarityAreaHeight / 2) * Math.cos(degrees))),(float) (centerY + ((similarityAreaHeight / 2) * Math.sin(degrees))),linePaint);

        //画字
        if (startProgress >= 0 && startProgress <= 33) {
            textPaint.setColor(blueColor);
        }else if (startProgress > 33  && startProgress <= 66){
            textPaint.setColor(orangeColor);
        } else if (startProgress > 66 && startProgress <= 100) {
            textPaint.setColor(redColor);
        }

        canvas.drawText(text,centerX ,centerY - (innerRadio / 4),textPaint);


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }


    @SuppressLint("HandlerLeak")
    class MyHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what >= 0 && msg.what + 5 <= progress) {
                startProgress += 5;
                handler.sendEmptyMessageDelayed(startProgress, handlerDelayTime);
            } else {
                startProgress = progress;
            }
            text = "相似度：" + startProgress + " %";
            arr = new float[text.length()];
            invalidate();
        }
    }


    /**
     * 移除handler，防止内存泄漏
     */
    public void onDestroy(){
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

}
