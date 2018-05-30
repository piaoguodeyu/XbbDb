package com.db.application;

import android.app.Application;

import com.xbbdb.dao.DbFactory;

/**
 * Created by zhangxiaowei on 18/5/30.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DbFactory.init();
    }
}
