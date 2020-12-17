/*
 * Copyright (C) 2013 www.418log.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xbbdb.orm.helper;

import android.database.sqlite.SQLiteDatabase;

import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.TableHelper;
import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.Id;
import com.xbbdb.orm.annotation.Table;
import com.xbbdb.sql.SqlDelete;
import com.xbbdb.sql.SqlInsert;
import com.xbbdb.sql.SqlQuery;
import com.xbbdb.sql.SqlUpdate;
import com.xbbdb.utils.XbbLogUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


/**
 * The Class DBImpl.
 * 2016/03/16张效伟修改
 *
 * @param <T> the generic type
 */
public class DBImpl<T> {
    private final String TAG = DBImpl.class.getSimpleName();
    /**
     * 锁对象
     */

    /**
     * The table name.
     */
    private String mTableName;

    /**
     * 自定义主键
     */

    private String idColumn;

    /**
     * The clazz.
     */
    private Class<T> clazz;

    /**
     * The all fields.
     */
    private List<Field> allFields;


    /**
     * The mSQLiteDatabase helper.
     */
    private DbFactory mSQLiteOpenHelper;

    public String getmTableName() {
        return mTableName;
    }


    public String getIdColumn() {
        return idColumn;
    }

    /**
     * 用一个对象实体初始化这个数据库操作实现类.
     *
     * @param clazz 映射对象实体
     */

    public DBImpl(Class<T> clazz) {
        this.mSQLiteOpenHelper = DbFactory.getInstance();

        if (clazz == null) {
//            this.clazz = ((Class<T>) ((ParameterizedType) super
//                    .getClass().getGenericSuperclass())
//                    .getActualTypeArguments()[0]);
            return;
        } else {
            this.clazz = clazz;
            this.clazz = TableHelper.getTableClass(clazz);
        }

        if (this.clazz.isAnnotationPresent(Table.class)) {
            Table table = this.clazz.getAnnotation(Table.class);
            this.mTableName = table.name();
        }

        // 加载所有字段
        this.allFields = TableHelper.joinFieldsOnlyColumn(this.clazz);

        // 找到主键
        for (Field field : this.allFields) {
            if (field.isAnnotationPresent(Id.class)) {
                Id column = field.getAnnotation(Id.class);
                this.idColumn = column.name();
                break;
            }
        }
    }

    /**
     * 描述：插入实体.
     *
     * @param entity the entity
     * @return the long
     */

    protected long insertAbs(T entity) {
        return new SqlInsert<T>(this.allFields, this.mTableName).insertAbs(entity, true);
    }

    /**
     * 描述：插入实体.
     *
     * @param entity the entity
     * @param flag   the flag
     * @return the long
     */
    protected long insertAbs(Object entity, boolean flag) {
        return new SqlInsert<T>(this.allFields, this.mTableName).insertAbs(entity, flag);
    }

    /**
     * 描述：插入列表
     */
    protected long insertListAbs(List<T> entityList) {
        return new SqlInsert<T>(this.allFields, this.mTableName).insertListAbs(entityList, true);
    }

    /**
     * 描述：插入列表
     */
    protected long insertListAbs(List<T> entityList, boolean flag) {
        return new SqlInsert<T>(this.allFields, this.mTableName).insertListAbs(entityList, true);

    }

    /**
     * 描述：按id删除.
     *
     * @param id the id
     */
    protected long deleteAbs(int id) {
        return new SqlDelete(this.clazz, this.allFields, this.mTableName, this.idColumn).deleteAbs(id + "");
    }

    /**
     * 删除集合
     *
     * @param ids
     * @return
     */
    protected int deleteListAbs(List<T> ids) {
        return new SqlDelete(this.clazz, this.allFields, this.mTableName, this.idColumn).deleteListAbs(ids);
    }

    protected List<T> deleteListReturnUnsuccessAbs(List<T> list) {
        return new SqlDelete(this.clazz, this.allFields, this.mTableName, this.idColumn).deleteListReturnUnsuccessAbs(list);
    }

    /**
     * 描述：按id删除.
     *
     * @param id the id
     */

    protected long deleteAbs(String id) {
        return new SqlDelete(this.clazz, this.allFields, this.mTableName, this.idColumn).deleteAbs(id);
    }

    /**
     * 描述：按id删除.
     *
     * @param ids the ids
     */
    public int deleteAbs(int[] ids) {
        return new SqlDelete(this.clazz, this.allFields, this.mTableName, this.idColumn).deleteAbs(ids);

    }

    /**
     * @param ids 根据指定的ID来删除数据,该实体类必须制定ID
     * @return
     */
    protected int deleteAbs(String[] ids) {

        return new SqlDelete(this.clazz, this.allFields, this.mTableName, this.idColumn).deleteAbs(ids);

    }

    /**
     * 描述：按条件删除数据
     */
    protected long deleteAbs(String whereClause, String[] whereArgs) {
        return new SqlDelete<>(this.clazz, this.allFields, this.mTableName, this.idColumn).deleteAbs(whereClause, whereArgs);
    }


    /**
     * 描述：清空数据
     */
    protected long deleteAllAbs() {
        return new SqlDelete(this.clazz, this.allFields, this.mTableName, this.idColumn).deleteAllAbs();
    }


    /**
     * @param entity 根据主键删除单条数据.,该实体类必须制定ID
     * @return
     */
    protected long deleteOneAbs(T entity) {
        return new SqlDelete<T>(this.clazz, this.allFields, this.mTableName, this.idColumn).delete(this.idColumn, entity);
    }

    protected long deleteOneByColumnAbs(String column, T entity) {
        return new SqlDelete<T>(this.clazz, this.allFields, this.mTableName, this.idColumn).delete(column, entity);
    }

    /**
     * 描述：更新实体.
     *
     * @param entity the entity
     * @return the long
     */
    protected long updateAbs(T entity) {
        return new SqlUpdate<T>(this.idColumn).update(this.idColumn, entity, allFields, mTableName);
    }

    protected long updateByColumnAbs(String column, T entity) {
        return new SqlUpdate<T>(this.idColumn).update(column, entity, allFields, mTableName);

    }


    /**
     * @param entityList 数据列表,ID主键
     * @return
     */
    protected long updateListAbs(List<T> entityList) {
        return new SqlUpdate<T>(this.idColumn).updateListAbs(entityList, allFields, mTableName);

    }

    /**
     * 描述：执行特定的sql.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     */
    public void execSqlAbs(String sql, Object[] selectionArgs) {
        try {
            if (selectionArgs == null) {
                mSQLiteOpenHelper.getWriteDatabase().execSQL(sql);
            } else {
                mSQLiteOpenHelper.getWriteDatabase().execSQL(sql, selectionArgs);
            }
            XbbLogUtil.d(TAG, "[execSql]: success" + getLogSql(sql, selectionArgs));
        } catch (Exception e) {
            XbbLogUtil.e(TAG, "[execSql] DB exception.");
            e.printStackTrace();
        } finally {
        }
    }


    /**
     * 打印当前sql语句.
     *
     * @param sql  sql语句，带？
     * @param args 绑定变量
     * @return 完整的sql
     */
    private String getLogSql(String sql, Object[] args) {
        if (args == null || args.length == 0) {
            return sql;
        }
        for (int i = 0; i < args.length; i++) {
            sql = sql.replaceFirst("\\?", "'" + String.valueOf(args[i]) + "'");
        }
        return sql;
    }


    protected int queryCountAbs() {
        return new SqlQuery<>(this.clazz, this.allFields, this.mTableName, this.idColumn).queryCountAbs();
    }

    /**
     * 描述：查询数量.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     * @return the int
     */
    protected int queryCountAbs(String sql, String[] selectionArgs) {
        return new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn).queryCountAbs(sql, selectionArgs);
    }

    private boolean queryList(Class<?> daoClasses, List<Object> list) throws IllegalAccessException {
        return new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn).queryList(daoClasses, list);
    }


    /**
     * 描述：简单一些的查询.
     *
     * @param where         the selection
     * @param selectionArgs the selection args
     * @return the list
     * @author: zhaoqp
     */
    protected List<T> queryListAbs(String where, String[] selectionArgs) {
        return new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn).queryListAbs(where, selectionArgs);
    }

    /**
     * 描述：查询所有数据.
     *
     * @return the list
     */
    protected List<T> queryListAbs() {
        return new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn).queryListAbs();
    }

    protected List<T> queryListAbs(String[] columns, String where,
                                   String[] selectionArgs, String groupBy, String having,
                                   String orderBy, String limit) {
        return (List<T>) new SqlQuery<>(this.clazz, this.allFields, this.mTableName, this.idColumn).queryListAbs(this.clazz, columns, where, selectionArgs, groupBy, having, orderBy, limit);
    }

    protected List<T> queryListAbs(int page, int pageSize) {
        String limit = (page - 1) * pageSize + "," + pageSize;
        XbbLogUtil.i(TAG, "DBImpl: queryList: [dddddddddddd]=" + limit);
        return (List<T>) new SqlQuery<T>(this.clazz, this.allFields, this.mTableName, this.idColumn).queryListAbs(this.clazz, null, null, null, null, null, null, limit);
    }

    protected List<T> queryListAbs(String limit) {
        XbbLogUtil.i(TAG, "DBImpl: queryList: [dddddddddddd]=" + limit);
        return (List<T>) new SqlQuery<T>(this.clazz, this.allFields, this.mTableName, this.idColumn).queryListAbs(this.clazz, null, null, null, null, null, null, limit);
    }

    /**
     * 描述：查询列表.
     *
     * @param columns       the columns
     * @param where         the selection
     * @param selectionArgs the selection args
     * @param groupBy       the group by
     * @param having        the having
     * @param orderBy       the order by
     * @param limit         the limit
     * @return the list
     */
    protected List<Object> queryListAbs(Class<?> daoClasses, String[] columns, String where,
                                        String[] selectionArgs, String groupBy, String having,
                                        String orderBy, String limit) {
        return new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn).queryListAbs(daoClasses, columns, where,
                selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * 描述：查询为map列表.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     * @return the list
     */
    protected List<Map<String, String>> queryMapListAbs(String sql, String[] selectionArgs) {
        return new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn)
                .queryMapListAbs(sql, selectionArgs);
    }

    /**
     * 描述：查询一条.
     *
     * @param id the id
     * @return the t
     */
    protected T queryOneAbs(int id) {
        return new SqlQuery<T>(this.clazz, this.allFields, this.mTableName, this.idColumn).queryOneAbs(id);
    }

    /**
     * 关联表查询
     *
     * @param daoClasses
     * @param colum
     * @param columValues
     * @return
     */
    protected List<Object> queryRelation(Class<?> daoClasses, String colum, String columValues) {
        return new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn).queryRelation(daoClasses, colum, columValues);
    }

    /**
     * 描述：查询一条.
     *
     * @param id the id
     * @return the t
     */
    protected T queryOneAbs(String id) {
        return new SqlQuery<T>(this.clazz, this.allFields, this.mTableName, this.idColumn).queryOneAbs(id);
    }

    /**
     * @param column 某一列的列明
     * @param data   某一列数据
     * @return
     */
    protected T queryOneAbs(String column, String data) {
        return new SqlQuery<T>(this.clazz, this.allFields, this.mTableName, this.idColumn).queryOneAbs(column, data);
    }

    /**
     * 描述：一种更灵活的方式查询，不支持对象关联，可以写完整的sql.
     *
     * @param sql           完整的sql如：selectAll * from a ,b where a.id=b.id and a.id = ?
     * @param selectionArgs 绑定变量值
     * @return the list
     */
    protected List<T> queryRawAbs(String sql, String[] selectionArgs) {
        return new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn).queryRawAbs(sql, selectionArgs, this.clazz);
    }

    public void closeReadDatabase() {
        mSQLiteOpenHelper.closeReadDatabase();
    }

    /**
     * 打开写事务
     */
    public void openWriteTransaction() {
        SQLiteDatabase database = DbFactory.getInstance().getWriteDatabase();
        try {
            if (database != null) {
                database.beginTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
            XbbLogUtil.i(TAG, "DBImpl: setWriteTransactionSuccessful: []="
                    + e);
        }
    }


    /**
     * 描述：获取写数据库，数据操作前必须调用
     *
     * @throws
     */
    protected void startWritableDatabase() {
        try {
            mSQLiteOpenHelper.openWriteDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            XbbLogUtil.i(TAG, "DBImpl: startWritableDatabase: [transaction]="
                    + e);
        } finally {
        }

    }

    /**
     * 描述：获取读数据库，数据操作前必须调用
     *
     * @throws
     */
    protected synchronized void startReadableDatabase() {
        try {
            mSQLiteOpenHelper.openReadDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            XbbLogUtil.i(TAG, "DBImpl: startReadableDatabase: [transaction]="
                    + e);
        }

    }


    /**
     * 描述：操作完成后设置事务成功后才能调用closeDatabase(true);
     *
     * @throws
     */
    protected void setWriteTransactionSuccessful() {
        SQLiteDatabase database = DbFactory.getInstance().getWriteDatabase();
        try {
            if (database != null) {
                database.setTransactionSuccessful();
            }
        } catch (Exception e) {
            e.printStackTrace();
            XbbLogUtil.i(TAG, "DBImpl: setWriteTransactionSuccessful: []="
                    + e);
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
    }

    /**
     * 描述：关闭数据库，数据操作后必须调用
     *
     * @throws
     */
    protected void closeWriteDatabase() {
        mSQLiteOpenHelper.closeWriteDatabase();
    }


}
