package com.cg.base.utils;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;


public class ToastUtils {
    private static Toast mToast = null;

    private static Toast mToastWithIcon = null;

    /**
     * 弱提示方法
     *
     * @param context 上下文对象
     * @param id      R文件引用的ID，具体是写在配置文件中的需要提示的内容
     * @param str     提示字符串结尾的内容，一般可以是标点，如果没有传空
     * @since V1.0
     */
    public static void showToast(Context context, int id, String str) {
        if (str == null || null == context) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), context.getString(id) + str, Toast.LENGTH_LONG);
            mToast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            mToast.setText(context.getString(id) + str);
        }
        mToast.show();
    }

    /**
     * 弱提示方法
     *
     * @param str 提示的内容
     * @since V1.0
     */
    public static void showToast(Context context, String str) {
        if (str == null || null == context) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), str, Toast.LENGTH_LONG);
            mToast.setGravity(Gravity.CENTER, 0, 200);
        } else {
            mToast.setText(str);
        }
        mToast.show();
    }


    /**
     * 自定义带图标弱提示方法
     *
     * @param context 上下文对象
     * @param xoffset 距离屏幕中心位置处的x轴方向距离，单位为px，建议使用工具类DisplayUtils.dip2px()将dp转换为px
     * @param yoffset 距离屏幕中心位置处的y轴方向距离，单位为px，建议使用工具类DisplayUtils.dip2px()将dp转换为px
     * @since V1.0
     */
    public static void showToastWithIconOnCenter(Context context, int
            xoffset, int yoffset,View view) {
        if (null == context) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        if (mToastWithIcon == null) {
            mToastWithIcon = new Toast(context);
            mToastWithIcon.setGravity(Gravity.CENTER, xoffset, yoffset);
            mToastWithIcon.setDuration(Toast.LENGTH_LONG);
            mToastWithIcon.setView(view);
        }
        mToastWithIcon.show();
    }

}
