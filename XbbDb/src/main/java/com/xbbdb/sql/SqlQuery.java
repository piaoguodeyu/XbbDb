package com.xbbdb.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
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
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangxiaowei on 16/11/18.
 */

public class SqlQuery<T> {
    private final String TAG = SqlQuery.class.getSimpleName();
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
     * 用一个对象实体初始化这个数据库操作实现类.
     */
    public SqlQuery(Class<T> clazz, List<Field> allFields,
                    String tableName, String idcolumn) {
        this.clazz = clazz;
        this.allFields = allFields;
        mTableName = tableName;
        idColumn = idcolumn;
    }

    public int queryCountAbs() {
        Cursor cursor = null;
        int count = 0;
        try {
            String sql = "select count(*) from " + this.mTableName;
            SQLiteStatement statement = DbFactory.getInstance().openReadDatabase().compileStatement(sql);
            count = (int) statement.simpleQueryForLong();
        } catch (Exception e) {
            LogUtil.e(TAG, "[queryCount] from DB exception");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return count;
    }

    /**
     * 描述：查询数量.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     * @return the int
     */
    public int queryCountAbs(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        int count = 0;
        try {
            LogUtil.d(TAG, "[queryCount]: " + getLogSql(sql, selectionArgs));
            cursor = DbFactory.getInstance().openReadDatabase().query(this.mTableName, null, sql, selectionArgs, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "[queryCount] from DB exception");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }
        return count;
    }

    public boolean queryList(Class<?> daoClasses, List<Object> list) throws IllegalAccessException {
        List<Field> allfield = getFiled(daoClasses);
        //需要判断是否有关联表
        for (Field childField : allfield) {
            String foreignKey = null;
            String type = null;
            /**
             * 父类列表属性与外键对应
             */
            String name = null;

            if (!childField.isAnnotationPresent(RelationDao.class)) {
                continue;
            }

            RelationDao relationDao = childField.getAnnotation(RelationDao.class);
            //获取外键列名
            foreignKey = relationDao.foreignKey();
            //关联类型
            type = relationDao.type();
            //操作类型
            name = relationDao.name();
            //设置可访问
            childField.setAccessible(true);

            //得到关联表的表名查询
            for (Object entity : list) {

                LogUtil.i(TAG, "DBImpl: queryListAbs: [llllllll]="
                        + entity);
                if (RelationsType.one2one.equals(type)) {
                    //一对一关系
                    //获取这个实体的表名
                    if (!childField.getType().isAnnotationPresent(Table.class)) {
                        break;
                    }

                    List<Object> relationsDaoList = new ArrayList<Object>();
                    Field[] relationsDaoEntityFields = childField.getType().getDeclaredFields();
                    datafield:
                    for (Field relationsDaoEntityField : relationsDaoEntityFields) {
                        relationsDaoEntityField.setAccessible(true);
                        Column relationsDaoEntityColumn = relationsDaoEntityField.getAnnotation(Column.class);
                        if (relationsDaoEntityColumn == null) {
                            LogUtil.i(TAG, "DBImpl: queryList:00000000000");
                            continue;
                        }
                        //获取外键的值作为关联表的查询条件
                        if (foreignKey.equals(relationsDaoEntityColumn.name())) {
                            //主表的用于关联表的foreignKey值
                            String value = "-1";

                            List<Field> prentfiled = getFiled(entity.getClass());
                            for (Field pFiled : prentfiled) {

                                if (!pFiled.isAnnotationPresent(Column.class)) {
                                    continue;
                                }
                                Column relationDao5 = pFiled.getAnnotation(Column.class);
                                //操作类型
                                String attributeName = relationDao5.name();
                                if (name.equals(attributeName)) {
                                    //设置可访问
                                    pFiled.setAccessible(true);
                                    value = String.valueOf(pFiled.get(entity));
                                    LogUtil.i(TAG, "DBImpl: queryListAbs: [vvvvvvvvv]="
                                            + value);

                                    relationsDaoList = queryRelation(childField.getType(), foreignKey, value);
                                    //查询数据设置给这个域
                                    if (relationsDaoList.size() > 0) {
                                        //获取关联表的对象设置值
                                        childField.set(entity, relationsDaoList.get(0));
                                    }
                                    break datafield;
                                }

                            }


                        }
                    }

                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //得到泛型里的class类型对象
                    Class listEntityClazz = null;
                    Class<?> fieldClass = childField.getType();
                    if (fieldClass.isAssignableFrom(List.class)) {
                        Type fc = childField.getGenericType();
                        if (fc == null) continue;
                        if (fc instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) fc;
                            listEntityClazz = (Class) pt.getActualTypeArguments()[0];
                        }
                    }

                    if (listEntityClazz == null) {
                        LogUtil.e(TAG, "对象模型需要设置List的泛型");
                        return true;
                    }

                    List<Object> relationsDaoList = new ArrayList<Object>();
                    Field[] declaredFields = listEntityClazz.getDeclaredFields();
                    datafield:
                    for (Field field : declaredFields) {
                        field.setAccessible(true);
                        Column relationsDaoEntityColumn = field.getAnnotation(Column.class);
                        //获取外键的值作为关联表的查询条件
                        if (relationsDaoEntityColumn != null && relationsDaoEntityColumn.name().equals(foreignKey)) {
                            String value = "-1";
                            List<Field> prentfiled = getFiled(entity.getClass());
                            for (Field pFiled : prentfiled) {
                                if (!pFiled.isAnnotationPresent(Column.class)) {
                                    continue;
                                }
                                Column relationDao5 = pFiled.getAnnotation(Column.class);
                                //操作类型
                                String attributeName = relationDao5.name();
                                if (name.equals(attributeName)) {
                                    //设置可访问
                                    pFiled.setAccessible(true);
                                    value = String.valueOf(pFiled.get(entity));
                                    LogUtil.i(TAG, "DBImpl: queryListAbs: [vvvvvvvvv]="
                                            + value);
                                    relationsDaoList = queryRelation(listEntityClazz, foreignKey, value);
                                    //查询数据设置给这个域
                                    if (relationsDaoList.size() > 0) {
                                        //获取关联表的对象设置值
                                        childField.set(entity, relationsDaoList);
                                    }
                                    break datafield;
                                }

                            }

                        }
                    }

                }
            }
        }
        return false;
    }

    /**
     * 描述：简单一些的查询.
     *
     * @param where         the selection
     * @param selectionArgs the selection args
     * @return the list
     * @author: zhaoqp
     */

    public List<T> queryListAbs(String where, String[] selectionArgs) {
        return (List<T>) queryListAbs(this.clazz, null, where, selectionArgs, null, null, null, null);
    }

    /**
     * 从游标中获得映射对象列表.
     *
     * @param list   返回的映射对象列表
     * @param cursor 当前游标
     * @return the list from cursor
     * @throws IllegalAccessException the illegal access exception
     * @throws InstantiationException the instantiation exception
     */
    private void getListFromCursor(Class<?> clazz, List<Object> list, Cursor cursor)
            throws IllegalAccessException, InstantiationException {
        while (cursor.moveToNext()) {
            Object entity = clazz.newInstance();
            // 加载所有字段
            List<Field> allFields = TableHelper.joinFields(entity.getClass().getDeclaredFields(),
                    entity.getClass().getSuperclass().getDeclaredFields());


            for (Field field : allFields) {
                Column column = null;
                if (field.isAnnotationPresent(Column.class)) {
                    column = field.getAnnotation(Column.class);

                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();

                    int c = cursor.getColumnIndex(column.name());
                    if (c < 0) {
                        continue; // 如果不存则循环下个属性值
                    } else if ((Integer.TYPE == fieldType)
                            || (Integer.class == fieldType)) {
                        field.set(entity, cursor.getInt(c));
                    } else if (String.class == fieldType) {
                        field.set(entity, cursor.getString(c));
                    } else if ((Long.TYPE == fieldType)
                            || (Long.class == fieldType)) {
                        field.set(entity, Long.valueOf(cursor.getLong(c)));
                    } else if ((Float.TYPE == fieldType)
                            || (Float.class == fieldType)) {
                        field.set(entity, Float.valueOf(cursor.getFloat(c)));
                    } else if ((Short.TYPE == fieldType)
                            || (Short.class == fieldType)) {
                        field.set(entity, Short.valueOf(cursor.getShort(c)));
                    } else if ((Double.TYPE == fieldType)
                            || (Double.class == fieldType)) {
                        field.set(entity, Double.valueOf(cursor.getDouble(c)));
                    } else if (Date.class == fieldType) {// 处理java.util.Date类型,update2012-06-10
                        Date date = new Date();
                        date.setTime(cursor.getLong(c));
                        field.set(entity, date);
                    } else if (Blob.class == fieldType) {
                        field.set(entity, cursor.getBlob(c));
                    } else if (Character.TYPE == fieldType) {
                        String fieldValue = cursor.getString(c);
                        if ((fieldValue != null) && (fieldValue.length() > 0)) {
                            field.set(entity, Character.valueOf(fieldValue.charAt(0)));
                        }
                    } else if ((Boolean.TYPE == fieldType) || (Boolean.class == fieldType)) {
                        String temp = cursor.getString(c);
                        if ("true".equals(temp) || "1".equals(temp)) {
                            field.set(entity, true);
                        } else {
                            field.set(entity, false);
                        }
                    }

                }
            }

            list.add(entity);
        }
    }

    /**
     * 描述：查询所有数据.
     *
     * @return the list
     */
    public List<T> queryListAbs() {
        return (List<T>) queryListAbs(this.clazz, null, null, null, null, null, null, null);
    }

    public List<T> queryListAbs(String[] columns, String where,
                                String[] selectionArgs, String groupBy, String having,
                                String orderBy, String limit) {
        return (List<T>) queryListAbs(this.clazz, columns, where, selectionArgs, groupBy, having, orderBy, limit);
    }

    public List<T> queryListAbs(int page, int pageSize) {
        String limit = (page - 1) * pageSize + "," + pageSize;
        LogUtil.i(TAG, "DBImpl: queryList: [dddddddddddd]=" + limit);
        return (List<T>) queryListAbs(this.clazz, null, null, null, null, null, null, limit);
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
    public List<Object> queryListAbs(Class<?> daoClasses, String[] columns, String where,
                                     String[] selectionArgs, String groupBy, String having,
                                     String orderBy, String limit) {

        List<Object> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            String tableName1 = getTableNeame(daoClasses);
            LogUtil.d(TAG, "[666666666] from " + tableName1 + " where " + where
                    + "(" + selectionArgs + ")" + " group by " + groupBy + " having " + having + " order by " + orderBy + " limit " + limit);
            cursor = DbFactory.getInstance().openReadDatabase().query(tableName1, columns, where,
                    selectionArgs, groupBy, having, orderBy, limit);
            getListFromCursor(daoClasses, list, cursor);
            //获取关联域的操作类型和关系类型
            queryList(daoClasses, list);
        } catch (Exception e) {
            LogUtil.e(this.TAG, "[queryList] from DB Exception" + e);
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return list;
    }

    /**
     * 描述：查询为map列表.
     *
     * @param sql           the sql
     * @param selectionArgs the selection args
     * @return the list
     */
    public List<Map<String, String>> queryMapListAbs(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            LogUtil.d(TAG, "[queryMapList]: " + getLogSql(sql, selectionArgs));
            cursor = DbFactory.getInstance().openReadDatabase().rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                Map<String, String> map = new HashMap<String, String>();
                for (String columnName : cursor.getColumnNames()) {
                    int c = cursor.getColumnIndex(columnName);
                    if (c < 0) {
                        continue; // 如果不存在循环下个属性值
                    } else {
                        map.put(columnName.toLowerCase(), cursor.getString(c));
                    }
                }
                retList.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "[queryMapList] from DB exception");
        } finally {
            closeCursor(cursor);
        }
        return retList;
    }

    /**
     * 描述：查询一条.
     *
     * @param id the id
     * @return the t
     */
    public T queryOneAbs(int id) {
        String selection = this.idColumn + " = ?";
        String[] selectionArgs = {Integer.toString(id)};
        LogUtil.d(TAG, "[queryOne]: selectAll * from " + this.mTableName + " where "
                + this.idColumn + " = '" + id + "'");
        List<Object> list = queryListAbs(this.clazz, null, selection, selectionArgs, null, null, null,
                null);
        if ((list != null) && (list.size() > 0)) {
            return (T) list.get(0);
        }
        return null;
    }


    /**
     * 关联表查询
     *
     * @param daoClasses
     * @param colum
     * @param columValues
     * @return
     */
    public List<Object> queryRelation(Class<?> daoClasses, String colum, String columValues) {
//        synchronized (lock) {
        String table = getTableNeame(daoClasses);
        String selection = colum + " = ?";
        String[] selectionArgs = {columValues};
        LogUtil.d(TAG, "[queryOneAbs]: selectAll * from " + table + " where "
                + colum + " = '" + columValues + "'");
        List<Object> list = queryListAbs(daoClasses, null, selection, selectionArgs, null, null, null,
                null);
        return list;
//        }
    }


    /**
     * 描述：查询一条.
     *
     * @param id the id
     * @return the t
     */
    public T queryOneAbs(String id) {
        String selection = this.idColumn + " = ?";
        String[] selectionArgs = {id};
        LogUtil.d(TAG, "[queryOne]: selectAll * from " + this.mTableName + " where "
                + this.idColumn + " = '" + id + "'");
        List<T> list = (List<T>) queryListAbs(this.clazz, null, selection, selectionArgs, null, null, null,
                null);
        if ((list != null) && (list.size() > 0)) {
            return list.get(0);
        }
        return null;
    }

    /**
     * @param column 某一列的列明
     * @param data   某一列数据
     * @return
     */
    public T queryOneAbs(String column, String data) {
        String selection = column + " = ?";
        String[] selectionArgs = {data};
        LogUtil.d(TAG, "[queryOne]: selectAll * from " + this.mTableName + " where "
                + this.idColumn + " = '" + column + "'");
        List<T> list = (List<T>) queryListAbs(this.clazz, null, selection, selectionArgs, null, null, null,
                null);
        if ((list != null) && (list.size() > 0)) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 描述：一种更灵活的方式查询，不支持对象关联，可以写完整的sql.
     *
     * @param sql           完整的sql如：selectAll * from a ,b where a.id=b.id and a.id = ?
     * @param selectionArgs 绑定变量值
     * @param clazz         返回的对象类型
     * @return the list
     */
    public List<T> queryRawAbs(String sql, String[] selectionArgs, Class<T> clazz) {

        List<Object> list = new ArrayList<Object>();
        Cursor cursor = null;
        try {
            LogUtil.d(TAG, "[queryRaw]: " + getLogSql(sql, selectionArgs));
            cursor = DbFactory.getInstance().openReadDatabase().rawQuery(sql, selectionArgs);
            getListFromCursor(clazz, list, cursor);
            //获取关联域的操作类型和关系类型
            queryList(this.clazz, list);
        } catch (Exception e) {
            LogUtil.e(this.TAG, "[queryRaw] from DB Exception." + e);
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return (List<T>) list;
    }

    /**
     * 获取所有属性
     *
     * @param daoClasses
     * @return
     */
    private List<Field> getFiled(Class<?> daoClasses) {
        // 加载所有字段
        return TableHelper.joinFields(daoClasses.getDeclaredFields(),
                daoClasses.getSuperclass().getDeclaredFields());
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
     * 描述：关闭游标.
     *
     * @param cursor the cursor
     */
    public void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
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

}
