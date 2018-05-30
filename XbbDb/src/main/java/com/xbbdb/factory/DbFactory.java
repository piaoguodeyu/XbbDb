package com.xbbdb.factory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xbbdb.global.DbConfig;
import com.xbbdb.orm.DBHelper;
import com.xbbdb.orm.helper.DbModel;
import com.xbbdb.utils.LogUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述：手机data/data下面的数据库
 *
 * @author zhangxiaowei
 * @version v2.0
 * @date：2016-10-31 修改
 */
public class DbFactory extends DBHelper {
    private final String TAG = DbFactory.class.getSimpleName();
    /**
     * 锁对象
     */
    private SQLiteDatabase mWriteDatabase;
    /**
     * 读
     */
    private SQLiteDatabase mReadDatabase;
    private AtomicInteger mCountWrite = new AtomicInteger();
    private AtomicInteger mCountRead = new AtomicInteger();
    // 数据库名
    private static String DBNAME;
    // 当前数据库的版本
    private static int DBVERSION;
    // 要初始化的表
    private static Class<?>[] mClazz;
    static DbFactory mDbInsideHelper;

    private DbFactory(Context context) {
        super(context, DBNAME, null, DBVERSION, mClazz);
    }

    public static DbFactory getInstance() {
        return mDbInsideHelper;
    }

    public static void init(DbConfig config) {
        if (mDbInsideHelper == null) {
            synchronized (DbFactory.class) {
                if (mDbInsideHelper == null) {
                    mClazz = config.getmClazz();
                    DBNAME = config.getDBNAME();
                    DBVERSION = config.getDBVERSION();
                    mDbInsideHelper = new DbFactory(config.getContext());
                }
            }
        }
    }

    /**
     * 关闭数据库
     */
    public boolean canCloseWriteDb() {
        if (mCountWrite.get() == 0) {
            return true;
        }
        return mCountWrite.decrementAndGet() == 0;
    }

    /**
     * 关闭数据库
     */
    public boolean canCloseReadDb() {
        if (mCountRead.get() == 0) {
            return true;
        }
        return mCountRead.decrementAndGet() == 0;
    }

    /**
     * 计算访问数据库库个数
     */
    private boolean writeDbIsOpen() {
        return mCountWrite.get() == 0;
    }

    /**
     * 计算访问数据库库个数
     */
    private boolean readDbIsOpen() {
        return mCountRead.get() == 0;
    }

    public SQLiteDatabase openReadDatabase() {
        synchronized (DbFactory.class) {
            try {
                mCountRead.incrementAndGet();
                if (mReadDatabase == null || !mReadDatabase.isOpen() && !readDbIsOpen()) {
                    mReadDatabase = getReadableDatabase();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i(TAG, "DbFactory: openWriteDatabase: []="
                        + e);
            }


            return mReadDatabase;
        }
    }

    /**
     * @return
     */
    public SQLiteDatabase getReadDatabase() {
        return mReadDatabase;
    }

    public SQLiteDatabase getWriteDatabase() {
        return mWriteDatabase;
    }

    public void onCreateWriteDatabase(SQLiteDatabase db) {
        synchronized (DBHelper.class) {
            mWriteDatabase = db;
            mReadDatabase = db;
        }
    }

    /**
     * 打开写的数据库
     *
     * @return
     */
    public SQLiteDatabase openWriteDatabase() {
        synchronized (DBHelper.class) {
            try {
                mCountWrite.incrementAndGet();
                if (mWriteDatabase == null || !mWriteDatabase.isOpen() && !writeDbIsOpen()) {
                    mWriteDatabase = getWritableDatabase();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i(TAG, "DbFactory: openWriteDatabase: []="
                        + e);
            }
            return mWriteDatabase;
        }

    }

    /**
     * 关闭写数据库
     */
    public void closeWriteDatabase() {
        synchronized (TAG) {
            try {
                if (canCloseWriteDb()) {
                    if (mWriteDatabase != null) {
                        LogUtil.i(TAG, "DBImpl: closeWriteDatabase: [ddddddd]="
                                + mWriteDatabase.isOpen() + "   " + writeDbIsOpen());
                        if (mWriteDatabase.isOpen()) {
                            mWriteDatabase.close();
                            mWriteDatabase = null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i(TAG, "DBImpl: closeWriteDatabase: [transaction]="
                        + e);
            }
        }

    }

    /**
     * 关闭读数据库
     */
    public synchronized void closeReadDatabase() {
        try {
            if (canCloseReadDb()) {
                if (mReadDatabase != null) {
                    LogUtil.i(TAG, "DBImpl: closeReadDatabase: [ddddddd]="
                            + mReadDatabase.isOpen() + "   " + readDbIsOpen());
                    if (mReadDatabase.isOpen()) {
                        mReadDatabase.close();
                        mReadDatabase = null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i(TAG, "DBImpl: closeReadDatabase: [transaction]="
                    + e);
        }
    }

    public <T> DbModel<T> openSession(Class<T> dbModel) {
//            Class[] argsClass = new Class[args.length];
//            for (int i = 0, j = args.length; i < j; i++) {
//                argsClass[i] = args[i].getClass();
//            }
//            Constructor cons = newoneClass.getConstructor(argsClass);
//            return cons.newInstance(args);
//            return dbModel.newInstance();
        return new DbModel<T>(dbModel);
    }
}
