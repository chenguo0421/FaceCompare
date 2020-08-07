package com.cg.face.album.contract;

import com.cg.base.base.mvp.BaseModel;
import com.cg.base.base.mvp.BasePresenter;
import com.cg.base.base.mvp.BaseView;


/**
 * @ProjectName: NVMS_3.0
 * @CreateDate: 2020-07-16 11:11:09
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public interface ImageDialogFMContract {
    interface IView extends BaseView {

    }

    abstract class IPresenter<V extends BaseView> extends BasePresenter<V> {

    }

    abstract class IModel extends BaseModel {

    }
}
