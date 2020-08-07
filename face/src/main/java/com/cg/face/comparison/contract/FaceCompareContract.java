package com.cg.face.comparison.contract;

import android.content.Context;

import com.arcsoft.face.FaceEngine;
import com.cg.base.base.mvp.BaseModel;
import com.cg.base.base.mvp.BasePresenter;
import com.cg.base.base.mvp.BaseView;


import io.reactivex.Observer;

/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020-08-05 19:26:03
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public interface FaceCompareContract {
    interface IView extends BaseView {

        void showToast(String string);

        boolean checkPermission(String[] neededPermissions);

        void onCompareSuccess(Integer integer);

        void onCompareError(String message);
    }

    abstract class IPresenter<V extends BaseView> extends BasePresenter<V> {

        public abstract void setSnapshotPath(String path);

        public abstract void setComparePath(String path);

        public abstract void compare();

        public abstract void initFaceEngine();

        public abstract void activeEngine();

        public abstract int getPremissionCode();
    }

    abstract class IModel extends BaseModel {
        public abstract void compare(Context context, FaceEngine faceEngine, String snapshotPath, String comparePath, Observer<Integer> integerObserver);

        public abstract void activeEngine(Context context,Observer<Integer> integerObserver);
    }
}
