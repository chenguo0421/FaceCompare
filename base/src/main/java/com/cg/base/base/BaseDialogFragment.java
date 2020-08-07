package com.cg.base.base;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.cg.base.R;
import com.cg.base.base.mvp.BasePresenter;
import com.cg.base.base.mvp.BaseView;

/**
 * @ProjectName: NVMS_3.0
 * @Package: com.pengantai.f_tvt_base.base
 * @ClassName: BaseDialogFragment
 * @CreateDate: 2020/6/9 18:55
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public abstract class BaseDialogFragment<V extends BaseView,P extends BasePresenter<V>> extends DialogFragment {

    public String fragmentTag = "";
    protected V mView = null;
    protected P mPresenter = null;
    private View statusBarView = null;
    private int orientation  = R.style.RightAnimation;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setWindowAnimations(orientation);
        View v = inflater.inflate(initLayoutId(), container,false);
        initViews(v);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPresenter == null) {
            mPresenter = createPresenter();
        }

        if (mView == null) {
            mView = createView();
        }

        mPresenter.attachView(mView);

        orientation = fragmentIOAnimation();

        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initListener();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getActivity().getResources().getColor(R.color.common_white,getResources().newTheme())));
        setDialogAttribute();
    }



    private void setDialogAttribute() {
        WindowManager.LayoutParams attribute = getDialog().getWindow().getAttributes();
        attribute.dimAmount = setOutSideAlpha();
        attribute.width = setDialogWidth();
        attribute.height = setDialogHeight();
        attribute.gravity = setGravity();
        getDialog().getWindow().setAttributes(attribute);


        //  dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }



    @Override
    public void show(FragmentManager manager, String tag) {
        if(isAdded()){
            DialogFragment fragment = (DialogFragment) manager.findFragmentByTag(tag);
            if (fragment != null && fragment.getDialog() != null && !fragment.getDialog().isShowing()) {
                fragment.getDialog().show();
            }
            return;
        }
        super.show(manager, tag);
    }

    @Override
    public void showNow(FragmentManager manager , String tag) {
        if(isAdded()){
            DialogFragment fragment = (DialogFragment) manager.findFragmentByTag(tag);
            if (fragment != null && fragment.getDialog() != null && !fragment.getDialog().isShowing()) {
                fragment.getDialog().show();
            }
            return;
        }
        super.showNow(manager, tag);
    }

    protected abstract P createPresenter();
    protected abstract V createView();
    protected abstract void initViews(View view);
    protected abstract int initLayoutId();
    protected abstract void initData();
    protected abstract void initListener();
    protected abstract int fragmentIOAnimation();
    protected abstract int setDialogWidth();
    protected abstract int setDialogHeight();
    protected abstract float setOutSideAlpha();
    protected abstract int setGravity();
    public abstract void setBundleExtra(Bundle bundle);


    @Override
    public void onDestroy() {
        if (mPresenter!=null){
            mPresenter.detachView();
        }
        super.onDestroy();
    }
}
