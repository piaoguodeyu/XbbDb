package com.xbbdb.orm.helper;

import com.xbbdb.global.SqlColum;

import java.util.List;
import java.util.Map;

/**
 * @param <T>
 * @author zhangxiaowei
 */
public class DbModel<T> extends DBImpl<T> {
    private final String TAG = DbModel.class.getSimpleName();

    public DbModel(Class<T> clazz) {
        super(clazz);
    }


    public T queryOne(int id) {
        startReadableDatabase();
        T result = super.queryOneAbs(id);
        closeReadDatabase();
        return result;
    }

    /**
     * 根据id查询某一条数据
     *
     * @param id
     * @return
     */
    public T queryOne(String id) {
        startReadableDatabase();
        T result = super.queryOneAbs(id);
        closeReadDatabase();
        return result;
    }

    /**
     * 根据某一列查询数据
     *
     * @param column
     * @param data
     * @return
     */
    public T queryOne(String column, String data) {
        startReadableDatabase();
        T result = super.queryOneAbs(column, data);
        closeReadDatabase();
        return result;
    }

    /**
     * 灵活使用sql语句进行查询
     *
     * @param sql
     * @param selectionArgs
     * @return
     */
    public List<T> queryRaw(String sql, String[] selectionArgs) {
        startReadableDatabase();
        List<T> result = super.queryRawAbs(sql, selectionArgs);
        closeReadDatabase();
        return result;
    }

    /**
     * @param sql
     * @param selectionArgs
     * @return
     */
    public List<T> queryRaw(SqlColum sql, String[] selectionArgs) {
        startReadableDatabase();
        String sql1 = sql.getSql().replace("TableName0", getmTableName());
        List<T> result = super.queryRawAbs(sql1, selectionArgs);
        closeReadDatabase();
        return result;
    }


    /**
     * 查找所有信息
     *
     * @return
     */
    public List<T> queryList() {
        startReadableDatabase();
        List<T> result = super.queryListAbs();
        closeReadDatabase();
        return result;
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    public List<T> queryList(int page, int pageSize) {
        startReadableDatabase();
        List<T> result = super.queryListAbs(page, pageSize);
        closeReadDatabase();
        return result;
    }

    /**
     * 根据自定义sql语句查询
     *
     * @param sql
     * @param selectionArgs
     */
    public void execSql(String sql, String[] selectionArgs) {
        synchronized (DbModel.class) {
            startWritableDatabase();
            openWriteTransaction();
            super.execSqlAbs(sql, selectionArgs);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
        }
    }

    /**
     * 查询某些列，并追加限制
     *
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return
     */
    public List<T> queryList(String[] columns, String selection,
                             String[] selectionArgs, String groupBy, String having,
                             String orderBy, String limit) {
        startReadableDatabase();
        List<T> result = super.queryListAbs(columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        closeReadDatabase();
        return result;
    }

    public List<T> queryList(String selection, String[] selectionArgs) {
        startReadableDatabase();
        List<T> result = super.queryListAbs(selection, selectionArgs);
        closeReadDatabase();
        return result;
    }

    /**
     * 根据实体类插入一条数据
     *
     * @param entity
     * @return
     */
    public long insert(T entity) {
        synchronized (DbModel.class) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.insertAbs(entity);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    /**
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     */
    public void synchronizedMethod() {
        startWritableDatabase();

    }

    /**
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     */
    public void unSynchronizedMethod() {
//        setWriteTransactionSuccessful();
        closeWriteDatabase();
    }

    /**
     * 手动对数据库进行上锁，
     * 则需手动调用{@link #synchronizedMethod}
     * 根据某一列，而非主键进行更新
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     *
     * @param column
     * @param entity
     * @return
     */
    public long updateByColumnNoLock(String column, T entity) {
        long result = super.updateByColumnAbs(column, entity);
        return result;
    }

    /**
     * 手动对数据库进行上锁，
     * 则需手动调用{@link #synchronizedMethod}
     * 根据其他列删除数据
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     *
     * @param column
     * @param entity
     * @return
     */
    public long deleteOneByColumnNoLock(String column, T entity) {
        long result = super.deleteOneByColumnAbs(column, entity);
        return result;
    }

    /**
     * @param id
     * @return
     */
    public T queryOneNoLock(String id) {
        T result = super.queryOneAbs(id);
        return result;
    }

    /**
     * 手动对数据库进行上锁，
     * 则需手动调用{@link #synchronizedMethod}
     * 复杂逻辑多次调用数据库，避免多次连接，省时
     *
     * @param entity
     * @return
     */
    public long insertNoLock(T entity) {
        long result = super.insertAbs(entity);
        return result;
    }

    public long insert(T entity, boolean flag) {
        synchronized (DbModel.class) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.insertAbs(entity, flag);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    public long insertList(List<T> entityList) {
        synchronized (DbModel.class) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.insertListAbs(entityList);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    /**
     * 不采用事务
     *
     * @param entityList
     * @return
     */
    public long insertListNoTransaction(List<T> entityList) {
        synchronized (DbModel.class) {
            startWritableDatabase();
            long result = super.insertListAbs(entityList);
//            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    public long insertList(List<T> entityList, boolean flag) {
        synchronized (DbModel.class) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.insertListAbs(entityList, flag);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    public long delete(int id) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.deleteAbs(id);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    public long delete(String id) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.deleteAbs(id);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    /**
     * 删除集合
     *
     * @param ids
     * @return
     */
    public List<T> deleteList(List<T> ids) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            List<T> result = super.deleteListReturnUnsuccessAbs(ids);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }


    public long delete(int[] ids) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.deleteAbs(ids);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    /**
     * @param whereArgs
     * @return
     */
    public long delete(String[] whereArgs) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.deleteAbs(whereArgs);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }


    /**
     * @return
     */
    public long deleteAll() {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.deleteAllAbs();
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    /**
     * @param data
     * @return
     */
    public long deleteOne(T data) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.deleteOneAbs(data);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    /**
     * 根据其他列删除数据
     *
     * @param column
     * @param entity
     * @return
     */
    public long deleteOneByColumn(String column, T entity) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.deleteOneByColumnAbs(column, entity);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }


    /**
     * 根据实体类删除一条数据，该实体类必须制定id
     *
     * @param entity
     * @return
     */
    public long update(T entity) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.updateAbs(entity);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    /**
     * @param entityList 数据列表,ID主键
     * @return
     */
    public long updateList(List<T> entityList) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.updateListAbs(entityList);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }


    /**
     * 根据某一列，而非主键进行更新
     *
     * @param column
     * @param entity
     * @return
     */
    public long updateByColumn(String column, T entity) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            long result = super.updateByColumnAbs(column, entity);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
            return result;
        }
    }

    public List<Map<String, String>> queryMapList(String sql,
                                                  String[] selectionArgs) {
        startReadableDatabase();
        List<Map<String, String>> result = super.queryMapListAbs(sql,
                selectionArgs);
        closeReadDatabase();
        return result;
    }

    /**
     * 查询某范围共有多少行
     *
     * @param where         要查询的信息
     * @param selectionArgs 空缺值
     * @return
     */
    public int queryCount(String where, String[] selectionArgs) {
        startReadableDatabase();
        int result = super.queryCountAbs(where, selectionArgs);
        closeReadDatabase();
        return result;
    }

    /**
     * 查询该表共有多少行
     *
     * @return
     */
    public int queryCount() {
        startReadableDatabase();
        int result = super.queryCountAbs();
        closeReadDatabase();
        return result;
    }

    /**
     * 执行sql语句
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     */
    public void execSql(String sql, Object[] selectionArgs) {
        synchronized (DbModel.this) {
            startWritableDatabase();
            openWriteTransaction();
            super.execSqlAbs(sql, selectionArgs);
            setWriteTransactionSuccessful();
            closeWriteDatabase();
        }
    }

}
