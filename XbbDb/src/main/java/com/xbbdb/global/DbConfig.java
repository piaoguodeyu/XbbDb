package com.xbbdb.global;

import android.content.Context;

/**
 * Created by zhangxiaowei on 18/5/30.
 */

public class DbConfig {
    Context context;
    /**
     * 数据库名
     */
    private String DBNAME;
    /**
     * 当前数据库的版本
     */
    private int DBVERSION;
    /**
     * 要初始化的表
     */
    private Class<?>[] mClazz;

    public String getDBNAME() {
        return DBNAME;
    }

    public int getDBVERSION() {
        return DBVERSION;
    }

    public Class<?>[] getmClazz() {
        return mClazz;
    }

    public Context getContext() {
        return context;
    }

    public DbConfig setContext(Context context) {
        this.context = context;
        return this;
    }

    public DbConfig setDBNAME(String DBNAME) {
        this.DBNAME = DBNAME;
        return this;
    }

    public DbConfig setDBVERSION(int DBVERSION) {
        this.DBVERSION = DBVERSION;
        return this;
    }

    public DbConfig setClazz(Class<?>[] mClazz) {
        this.mClazz = mClazz;
        return this;
    }

}
