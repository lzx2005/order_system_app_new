package com.lzx2005.system.order.application;

import android.app.Application;

/**
 * Created by john on 2017/5/7.
 */

public class ApplicationData extends Application {
    private String rootUrl;

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    @Override
    public void onCreate() {
        //初始化一些全局变量
        //rootUrl = "";
        super.onCreate();
    }
}
