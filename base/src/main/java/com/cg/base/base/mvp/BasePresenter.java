package com.cg.base.base.mvp;

/**
 * @ProjectName: NVMS_3.0
 * @Package: com.pengantai.f_tvt_base.base.mvp
 * @ClassName: BasePresenter
 * @CreateDate: 2020/5/23 17:12
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public abstract class BasePresenter<V extends BaseView> {
    protected final String TAG = this.getClass().getName();
    private V mView;

    public V getView(){
        return mView;
    }

    public void attachView(V view){
        mView = view;
    }

    public void detachView(){
        mView = null;
        onDestroy();
    }

   public void onDestroy(){}
}
