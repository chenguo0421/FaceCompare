package com.cg.base.base;

import android.app.Application;

public class BaseApplication extends Application {
    private static BaseApplication application;

    public static BaseApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

    }

    private boolean isDebug() {
        return true;
    }

}
