package com.xbbdb.factory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xbbdb.global.DbConfig;
import com.xbbdb.orm.DBHelper;
import com.xbbdb.orm.helper.DbModel;
import com.xbbdb.utils.LogUtil;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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
    private SQLiteDatabase mSQLiteDatabase;
    private final ReentrantLock lock = new ReentrantLock();
    private static AtomicInteger mAtomicInteger = new AtomicInteger();
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
    public boolean canCloseDb() {
        return mAtomicInteger.decrementAndGet() == 0;
    }

    /**
     * 计算访问数据库库个数
     */
    public void openDb() {
        mAtomicInteger.incrementAndGet();
        getDatabase();
    }

    /**
     * 计算访问数据库库个数
     */
    public boolean isOpenDb() {
        return mAtomicInteger.get() == 0;
    }


    public synchronized SQLiteDatabase getDatabase() {
        try {
            lock.lock();
            if (mSQLiteDatabase == null || !mSQLiteDatabase.isOpen() && !isOpenDb()) {
                mSQLiteDatabase = getWritableDatabase();
            }
        } catch (Exception e) {
            LogUtil.i(TAG, "DbFactory: getDatabase: []="
                    + e);
        } finally {
            lock.unlock();
        }


        return mSQLiteDatabase;
    }

    public void closeDatabase() {
        try {
            if (canCloseDb()) {
                if (mSQLiteDatabase != null) {
                    LogUtil.i(TAG, "DBImpl: closeDatabase: [ddddddd]="
                            + mSQLiteDatabase.isOpen() + "   " + isOpenDb());
                    if (mSQLiteDatabase.isOpen()) {
                        mSQLiteDatabase.close();
                        mSQLiteDatabase = null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i(TAG, "DBImpl: closeDatabase: [transaction]="
                    + e);
        }
    }


    public <T> DbModel<T> openSession(Class<T> dbModel) {
        try {
            return new DbModel<T>(dbModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
