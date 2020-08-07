package com.cg.base.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.cg.base.R;
import com.cg.base.base.mvp.BasePresenter;
import com.cg.base.base.mvp.BaseView;
import com.cg.base.utils.ToastUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public abstract class BaseActivity<V extends BaseView,P extends BasePresenter<V>> extends AppCompatActivity {
    protected V mView;
    protected P mPresenter;
    protected final String TAG = this.getClass().getName();
    private AlertDialog mAlertDialog;
    private Disposable dispose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(this).inflate(initLayoutId(),null);
        setContentView(view);
        if (mPresenter == null){
            mPresenter = createPresenter();
        }
        if (mView == null){
            mView = createView();
        }
        mPresenter.attachView(mView);

        initView(view);
        initData();
        initListener();
    }

    public void showProgressDialog(long timeOut) {
        showProgressDialog(BaseActivity.this.getResources().getString(R.string.loading), timeOut);
    }

    public void showProgressDialog(String tip,long timeOut) {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(BaseActivity.this, R.style.CustomProgressDialog).create();
        }

        View loadView = LayoutInflater.from(BaseActivity.this).inflate(R.layout.custom_progress_dialog_view, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        TextView tvTip = loadView.findViewById(R.id.tvTip);
        tvTip.setText(tip);

        mAlertDialog.show();

        dispose = Observable.interval(0,1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (aLong >= timeOut && dispose!=null && !dispose.isDisposed()){
                        ToastUtils.showToast(BaseActivity.this,BaseActivity.this.getResources().getString(R.string.loading_timeout));
                        dismiss();
                    }
                });
    }

    public void dismiss() {
        if (dispose != null && !dispose.isDisposed()) {
            dispose.dispose();
            dispose = null;
        }
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

    protected abstract void initView(View view);
    protected abstract void initData();
    protected abstract void initListener();
    protected abstract int initLayoutId();
    protected abstract P createPresenter();
    protected abstract V createView();


    @Override
    protected void onStop() {
        dismiss();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        dismiss();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }

}
