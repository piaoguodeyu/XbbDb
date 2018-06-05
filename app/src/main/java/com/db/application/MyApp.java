package com.db.application;

import android.app.Application;

import com.db.table.Saff;
import com.db.table.User;
import com.xbbdb.factory.DbFactory;
import com.xbbdb.global.DbConfig;


/**
 * Created by zhangxiaowei on 18/5/30.
 */

public class MyApp extends Application {

    private Class<?>[] clazz = {User.class, Saff.class};

    @Override
    public void onCreate() {
        super.onCreate();
        DbConfig config = new DbConfig();
        config.setContext(getApplicationContext())
                .setDBNAME("xbb").setDBVERSION(16)
                .setClazz(clazz);
        DbFactory.init(config);
    }
}
