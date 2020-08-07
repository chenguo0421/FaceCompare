package com.cg.face.comparison.presenter;

import android.Manifest;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.cg.face.R;
import com.cg.face.comparison.contract.FaceCompareContract;
import com.cg.face.comparison.model.FaceCompareModel;


import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @ProjectName: NVMS_3.0
 * @CreateDate:  2020-08-05 19:26:03
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class FaceComparePresenter extends FaceCompareContract.IPresenter<FaceCompareContract.IView> {

    private final FaceCompareContract.IModel model;
    private String snapshotPath;
    private String comparePath;
    private FaceEngine faceEngine;
    private int faceEngineCode;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    // 在线激活所需的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    public FaceComparePresenter(){
        model = new FaceCompareModel();
    }

    @Override
    public void initFaceEngine() {
        faceEngine = new FaceEngine();
        faceEngineCode = faceEngine.init(getView().getBaseActivity(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, 6, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);
        Log.i(TAG, "initEngine: init " + faceEngineCode);
        if (faceEngineCode != ErrorInfo.MOK) {
            getView().showToast(getView().getBaseActivity().getString(R.string.face_warr_face_engin_init_failed));
        }
    }

    /**
     * 激活引擎
     */
    @Override
    public void activeEngine() {
        if (!getView().checkPermission(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(getView().getBaseActivity(), NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }

        model.activeEngine(getView().getBaseActivity(),new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer activeCode) {
                if (activeCode == ErrorInfo.MOK) {
                    getView().showToast(getView().getBaseActivity().getString(R.string.active_success));
                } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                    getView().showToast(getView().getBaseActivity().getString(R.string.already_activated));
                } else {
                    getView().showToast(getView().getBaseActivity().getString(R.string.active_failed, activeCode));
                }

                ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                int res = FaceEngine.getActiveFileInfo(getView().getBaseActivity(), activeFileInfo);
                if (res == ErrorInfo.MOK) {
                    Log.i(TAG, activeFileInfo.toString());
                    initFaceEngine();
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });


    }

    @Override
    public int getPremissionCode() {
        return ACTION_REQUEST_PERMISSIONS;
    }


    @Override
    public void setSnapshotPath(String path) {
        this.snapshotPath = path;
    }

    @Override
    public void setComparePath(String path) {
        this.comparePath = path;
    }

    @Override
    public void compare() {
        if (snapshotPath == null || snapshotPath.isEmpty()) {
            getView().showToast(getView().getBaseActivity().getString(R.string.face_warr_snapshot_path_empty));
            return;
        }
        if (comparePath == null || comparePath.isEmpty()) {
            getView().showToast(getView().getBaseActivity().getString(R.string.face_warr_compare_path_empty));
            return;
        }
        getView().getBaseActivity().showProgressDialog(60);
        model.compare(getView().getBaseActivity(),faceEngine,snapshotPath,comparePath, new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                if (getView() != null) {
                    getView().onCompareSuccess(integer);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (getView() != null) {
                    getView().onCompareError(e.getMessage());
                    getView().getBaseActivity().dismiss();
                }
            }

            @Override
            public void onComplete() {
                if (getView() != null) {
                    getView().getBaseActivity().dismiss();
                }
            }
        });
    }
}
