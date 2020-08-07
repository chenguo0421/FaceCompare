package com.cg.face.album.presenter;


import com.cg.face.album.contract.ImageDialogFMContract;
import com.cg.face.album.model.ImageDialogFMModel;

/**
 * @ProjectName: NVMS_3.0
 * @CreateDate:  2020-07-16 11:11:09
 * @Author: ChenGuo
 * @Description: java类作用描述
 * @Version: 1.0
 */
public class ImageDialogFMPresenter extends ImageDialogFMContract.IPresenter<ImageDialogFMContract.IView> {

    private final ImageDialogFMContract.IModel model;

    public ImageDialogFMPresenter(){
        model = new ImageDialogFMModel();
    }

}
