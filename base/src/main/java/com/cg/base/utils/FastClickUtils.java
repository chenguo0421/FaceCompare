package com.cg.base.utils;

/**
 * @ProjectName: NVMS_3.0
 * @Package: com.pengantai.f_tvt_base.utils
 * @ClassName: FaseClickUtils
 * @CreateDate: 2020/6/4 19:54
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FastClickUtils {

    private static long mLastClickTime;

    private static long customLastClickTime;

    private static int clickCount = 0;
    private static long clickTime;

    /**
     * 此方法用于禁止快速点击
     *
     * @return
     */
    public static boolean isFastClick(long interval) {
        // 当前时间
        long currentTime = System.currentTimeMillis();
        // 两次点击的时间差
        long time = currentTime - customLastClickTime;
        if (0 < time && time < interval) {
            return true;
        }
        customLastClickTime = currentTime;
        return false;
    }


    /**
     * 此方法用于禁止快速点击
     *
     * @return
     */

    public static boolean isFastClick() {
        // 当前时间
        long currentTime = System.currentTimeMillis();
        // 两次点击的时间差
        long time = currentTime - mLastClickTime;
        if (0 < time && time < 500) {
            return true;
        }
        mLastClickTime = currentTime;
        return false;
    }

    /**
     * 是否是连续的快速点击
     * @param interval 时间间隔
     * @return
     */
    public static boolean isFastClick(int count,int interval) {
        // 当前时间
        long currentTime = System.currentTimeMillis();
        // 两次点击的时间差
        long time = currentTime - clickTime;
        if (0 < time && time < interval) {
            clickCount ++;
        }else {
            clickCount = 0;
        }
        clickTime = currentTime;
        if (clickCount >= count){
            clickCount = 0;
            return true;
        }
        return false;
    }
}
