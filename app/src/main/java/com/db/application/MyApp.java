package com.db.application;

import android.app.Application;

import com.db.table.Saff;
import com.db.table.User;
import com.xbbdb.factory.DbFactory;
import com.xbbdb.global.DbConfig;
import com.xbbdb.orm.helper.DbModel;


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
                .setDBNAME("test.db").setDBVERSION(1)
                .setClazz(clazz);
        DbFactory.init(config);
        DbModel<User> dbModel=DbFactory.getInstance().openSession(User.class);
        dbModel.queryList();
        dbModel.deleteOne(new User());
        dbModel.update(new User());
        dbModel.insert(new User());
    }
}
