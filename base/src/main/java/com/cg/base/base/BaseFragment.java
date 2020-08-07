package com.cg.base.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cg.base.base.mvp.BasePresenter;
import com.cg.base.base.mvp.BaseView;


/**
 * @ProjectName: NVMS_3.0
 * @Package: com.pengantai.f_tvt_base.base
 * @ClassName: BaseFragment
 * @CreateDate: 2020/5/23 18:42
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public abstract class BaseFragment<V extends BaseView,P extends BasePresenter<V>> extends DialogFragment {
    protected V mView;
    protected P mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mPresenter == null) {
            mPresenter = createPresenter();
        }

        if (mView == null) {
            mView = createView();
        }

        mPresenter.attachView(mView);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(initLayoutId(), container, false);
        initViews(v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initListener();
    }




    protected abstract P createPresenter();
    protected abstract V createView();
    protected abstract void initViews(View view);
    protected abstract int initLayoutId();
    protected abstract void initData();
    protected abstract void initListener();
    public abstract void setBundleExtra(Bundle bundle);


    @Override
    public void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }
}
