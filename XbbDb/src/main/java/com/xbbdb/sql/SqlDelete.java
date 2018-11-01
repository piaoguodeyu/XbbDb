package com.xbbdb.sql;

import android.text.TextUtils;

import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.TableHelper;
import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.RelationDao;
import com.xbbdb.orm.annotation.RelationsType;
import com.xbbdb.orm.annotation.Table;
import com.xbbdb.utils.LogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by zhangxiaowei on 16/11/18.
 */

public class SqlDelete<T> {
    private final String TAG = SqlDelete.class.getSimpleName();
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
    SqlQuery mSqlQuery;

    /**
     * 用一个对象实体初始化这个数据库操作实现类.
     */
    public SqlDelete(Class<T> clazz, List<Field> allFields,
                     String tableName, String idcolumn) {
        this.clazz = clazz;
        this.allFields = allFields;
        mTableName = tableName;
        idColumn = idcolumn;
        mSqlQuery = new SqlQuery(this.clazz, this.allFields, this.mTableName, this.idColumn);
    }


    /**
     * 删除集合
     *
     * @param ids
     * @return
     */
    public int deleteListAbs(List<T> ids) {
        if (ids == null || ids.size() == 0) {
            return 0;
        }
        int rows = -1;
        try {
            for (T data : ids) {
                rows += delete(this.idColumn, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return rows;
    }

    public List<T> deleteListReturnUnsuccessAbs(List<T> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        int rows = -1;
        try {
            for (int i = 0; i < list.size(); ) {
                T data = list.get(0);
                rows += delete(this.idColumn, data);
                list.remove(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i(TAG, "DBImpl: deleteListReturnUnsuccessAbs: [list]="
                    + e);
        } finally {
        }
        return list;
    }

    /**
     * 描述：按id删除.
     *
     * @param id the id
     */
    public long deleteAbs(String id) {
        long rows = -1;
        try {
            rows = deleteAbs(this.clazz, this.idColumn, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return rows;
    }

    /**
     * 可以删除关联表信息
     *
     * @param clazz
     * @param coloum
     * @param id
     * @return
     */
    private long deleteAbs(Class<?> clazz, String coloum, String id) {
        long rows = -1;
        try {
            String tablename = getTableNeame(clazz);
            String where = coloum + " = ?";
            String[] whereValue = {id};
            LogUtil.d(TAG, "[delete]: delelte from " + tablename + " where "
                    + where.replace("?", id));
            List<Object> listdata = mSqlQuery.queryRelation(clazz, coloum, id);
            rows = DbFactory.getInstance().getWriteDatabase().delete(tablename, where, whereValue);
            if (listdata == null || listdata.size() == 0) {
                return rows;
            }
            List<Field> fff = getFiled(clazz);
            for (Field childField : fff) {
                String foreignKey = null;
                String type = null;
                String action = null;
                /**
                 * 父类列表属性与外键对应
                 */
                String name = null;
                if (!childField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }
                RelationDao relationDao1 = childField.getAnnotation(RelationDao.class);
                //获取外键列名
                foreignKey = relationDao1.foreignKey();
                //关联类型
                type = relationDao1.type();
                //操作类型
                name = relationDao1.name();
                //设置可访问
                childField.setAccessible(true);

                for (Object data : listdata) {
                    List<Field> dafiled = getFiled(data.getClass());
                    datafield:
                    for (Field relationsDaoField : dafiled) {
                        if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                            continue;
                        }
                        RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
                        //设置可访问
                        relationsDaoField.setAccessible(true);
                        if (RelationsType.one2one.equals(relationDao.type())) {

                            //获取外键的值作为关联表的查询条件
                            if (foreignKey.equals(relationDao.name())) {
                                //主表的用于关联表的foreignKey值
                                String value = "-1";

                                for (Field pFiled : dafiled) {

                                    if (!pFiled.isAnnotationPresent(Column.class)) {
                                        continue;
                                    }
                                    Column relationDao5 = pFiled.getAnnotation(Column.class);
                                    //操作类型
                                    String attributeName = relationDao5.name();
                                    if (name.equals(attributeName)) {
                                        //设置可访问
                                        pFiled.setAccessible(true);
                                        value = String.valueOf(pFiled.get(data));
                                        LogUtil.i(TAG, "DBImpl: queryListAbs: [vvvvvvvvv666666]="
                                                + value);
                                        String values = String.valueOf(relationsDaoField.get(data));
                                        rows += deleteAbs(relationsDaoField.getType(), relationDao.foreignKey(), values);

                                        break datafield;
                                    }

                                }


                            }

                        } else if (RelationsType.one2many.equals(relationDao.type())) {
                            Class<?> listEntityClazz = null;
                            Class<?> fieldClass = relationsDaoField.getType();
                            if (fieldClass.isAssignableFrom(List.class)) {
                                Type fc = relationsDaoField.getGenericType();
                                if (fc == null) continue;
                                if (fc instanceof ParameterizedType) {
                                    ParameterizedType pt = (ParameterizedType) fc;
                                    listEntityClazz = (Class) pt.getActualTypeArguments()[0];
                                }
                            }
                            if (foreignKey.equals(relationDao.name())) {
                                //主表的用于关联表的foreignKey值
                                String value = "-1";

                                for (Field pFiled : dafiled) {
                                    if (!pFiled.isAnnotationPresent(Column.class)) {
                                        continue;
                                    }
                                    Column relationDao5 = pFiled.getAnnotation(Column.class);
                                    //操作类型
                                    String attributeName = relationDao5.name();
                                    if (name.equals(attributeName)) {
                                        //设置可访问
                                        pFiled.setAccessible(true);
                                        value = String.valueOf(pFiled.get(data));
                                        LogUtil.i(TAG, "DBImpl: queryListAbs: [vvvvvvvvv00000]="
                                                + value);
                                        String values = String.valueOf(relationsDaoField.get(data));
//
                                        rows += deleteAbs(listEntityClazz, relationDao.foreignKey(), values);

                                        break datafield;
                                    }

                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i(TAG, "DBImpl: deleteAbs: [clazz, coloum, id]="
                    + e);
        } finally {
        }
        return rows;
    }

    /**
     * 描述：按id删除.
     *
     * @param ids the ids
     */
    public int deleteAbs(int[] ids) {
        int rows = -1;
        try {
            if (ids.length > 0) {
                StringBuilder builder = new StringBuilder(this.idColumn);
                builder.append(" in (");
                for (int i = 0; i < ids.length; i++) {
                    builder.append(ids[i])
                            .append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append(")");
                LogUtil.i(TAG, "DBImpl: delete: [ids]="
                        + builder.toString());
                List<T> list = (List<T>) mSqlQuery.queryListAbs(this.clazz, null, builder.toString(), null, null, null, null, null);
                rows = deleteListAbs(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i(TAG, "DBImpl: delete: [ids]="
                    + e);
        } finally {
        }
        return rows;
    }

    /**
     * @param ids 根据指定的ID来删除数据,该实体类必须制定ID
     * @return
     */
    public int deleteAbs(String[] ids) {
        int rows = -1;
        try {
            if (ids.length > 0) {
                StringBuilder builder = new StringBuilder(this.idColumn);
                builder.append(" in (");
                for (int i = 0; i < ids.length; i++) {
                    builder.append(ids[i])
                            .append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append(")");
                LogUtil.i(TAG, "DBImpl: delete: [ids]="
                        + builder.toString());
                List<T> list = (List<T>) mSqlQuery.queryListAbs(this.clazz, null, builder.toString(), null, null, null, null, null);
                rows = deleteListAbs(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i(TAG, "DBImpl: delete: [ids]="
                    + e);
        } finally {
        }


        return rows;

    }


    /**
     * 描述：按条件删除数据
     */
    public long deleteAbs(String whereClause, String[] whereArgs) {
        long rows = -1;
        try {
            String mLogSql = getLogSql(whereClause, whereArgs);
            if (!TextUtils.isEmpty(mLogSql)) {
                mLogSql = " where " + mLogSql;
            }
            LogUtil.d(TAG, "[delete]: delete from " + this.mTableName + mLogSql);
            List<T> list = (List<T>) mSqlQuery.queryListAbs(this.clazz, null, whereClause, whereArgs, null, null, null, null);
            rows = deleteListAbs(list);

        } catch (Exception e) {
            LogUtil.i(TAG, "DBImpl: deleteAbs: [whereClause, whereArgs]="
                    + e);
            e.printStackTrace();
        } finally {
        }
        return rows;
    }

    private long deleteAllData(Class<?> clazz) {
        long rows = -1;
        String tablename = getTableNeame(clazz);
        rows = DbFactory.getInstance().getWriteDatabase().delete(tablename, null, null);
        LogUtil.i(TAG, "DBImpl: deleteAll: [deleteAllData=]=" + tablename);

        for (Field relationsDaoField : getFiled(clazz)) {
            if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                continue;
            }
            RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
            if (RelationsType.one2one.equals(relationDao.type())) {
                //设置可访问
                rows += deleteAllData(relationsDaoField.getType());


            } else if (RelationsType.one2many.equals(relationDao.type())) {
                Class<?> listEntityClazz = null;
                Class<?> fieldClass = relationsDaoField.getType();
                if (fieldClass.isAssignableFrom(List.class)) {
                    Type fc = relationsDaoField.getGenericType();
                    if (fc == null) continue;
                    if (fc instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) fc;
                        listEntityClazz = (Class) pt.getActualTypeArguments()[0];
                    }
                }
                rows += deleteAllData(listEntityClazz);
            }

        }
        return rows;
    }

    /**
     * 描述：清空数据
     */
    public long deleteAllAbs() {
        long rows = -1;
        try {
            LogUtil.i(TAG, "DBImpl: deleteAll: [mTableName]=" + mTableName);
            rows = deleteAllData(this.clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }


    public long delete(String column, T entity) {
        long rows = -1;
        try {
            List<Field> list = getFiled(entity.getClass());
            for (Field relationsDaoField : list) {
                Column column1 = relationsDaoField.getAnnotation(Column.class);
                if (column.equals(column1.name())) {
                    relationsDaoField.setAccessible(true);
                    String str = (String) relationsDaoField.get(entity);
                    LogUtil.i(TAG, "DBImpl: : [deleteOne0000= delete from " + mTableName
                            + " where " + column + "= " + str);
                    rows = deleteAbs(entity.getClass(), column, str);
                }
            }


        } catch (Exception e) {
            LogUtil.i(TAG, "DBImpl: delete: [column, entity]="
                    + e);
            e.printStackTrace();
        }
        return rows;

    }

    /**
     * @param entity 根据主键删除单条数据.,该实体类必须制定ID
     * @return
     */
    public long deleteOneAbs(T entity) {
        return delete(this.idColumn, entity);
    }

    public long deleteOneByColumnAbs(String column, T entity) {
        return delete(column, entity);
    }

    /**
     * 获取所有属性
     *
     * @param daoClasses
     * @return
     */
    private List<Field> getFiled(Class<?> daoClasses) {
        // 加载所有字段
        return TableHelper.joinFieldsOnlyColumn(daoClasses);
    }

    /**
     * 获取表名
     *
     * @param daoClasses
     * @return
     */
    private String getTableNeame(Class<?> daoClasses) {
        String tablename = "";
        LogUtil.i(TAG, "DBImpl: getTableNeame: [ccccccc]="
                + daoClasses);
        if (daoClasses.isAnnotationPresent(Table.class)) {
            Table table = daoClasses.getAnnotation(Table.class);
            tablename = table.name();
        }
        if (TextUtils.isEmpty(tablename)) {
            LogUtil.i(TAG, "DaoConfig: DaoConfig: [daoClasses]="
                    + "想要映射的实体[" + daoClasses.getName() + "],未注解@Table(name=\"?\"),被跳过");

        }
        return tablename;
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

}
