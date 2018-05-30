package com.xbbdb.sql;

import android.content.ContentValues;
import android.text.TextUtils;

import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.TableHelper;
import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.RelationDao;
import com.xbbdb.orm.annotation.RelationsType;
import com.xbbdb.orm.annotation.Table;
import com.xbbdb.utils.LogUtil;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zhangxiaowei on 16/11/18.
 */

public class SqlInsert<T> {
    private final String TAG = SqlInsert.class.getSimpleName();
    /**
     * The Constant METHOD_INSERT.
     */
    private final int METHOD_INSERT = 0;

    /**
     * The Constant TYPE_NOT_INCREMENT.
     */
    private final int TYPE_NOT_INCREMENT = 0;

    /**
     * The Constant TYPE_INCREMENT.
     */
    private final int TYPE_INCREMENT = 1;

    /**
     * 锁对象
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * The table name.
     */
    private String mTableName;

    /**
     * The all fields.
     */
    private List<Field> allFields;

    /**
     * 用一个对象实体初始化这个数据库操作实现类.
     */
    public SqlInsert(List<Field> allFields,
                     String tableName) {
        this.allFields = allFields;
        mTableName = tableName;
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
     * 描述：插入实体.
     *
     * @param entity the entity
     * @return the long
     */

    public long insertAbs(T entity) {
        return insertAbs(entity, true);
    }

    /**
     * 描述：插入实体.
     *
     * @param entity the entity
     * @param flag   the flag
     * @return the long
     */

    public long insertAbs(Object entity, boolean flag) {
        String sql = null;
        long row = 0L;
        try {
            ContentValues cv = new ContentValues();
            if (flag) {
                // id自增
                sql = setContentValues(entity, cv, TYPE_INCREMENT, METHOD_INSERT);
            } else {
                // id需指定
                sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);
            }

            String tablename = getTableNeame(entity.getClass());
            LogUtil.i(TAG, "DBImpl: insert: [*********"
                    + "[insert]: insert into " + tablename + " " + sql);
            row = DbFactory.getInstance().getDatabase().insert(tablename, null, cv);

            //获取关联域的操作类型和关系类型
            String foreignKey = null;
            String type = null;
            String action = null;
            List<Field> filed = getFiled(entity.getClass());
            //需要判断是否有关联表
            for (Field relationsDaoField : filed) {
                RelationDao re = relationsDaoField.getAnnotation(RelationDao.class);
                if (re != null) {
                    LogUtil.i(TAG, "DBImpl: insertAbs: [entity, flag]="
                            + re.foreignKey());
                }

                if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }
                RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
                //获取外键列名
                foreignKey = relationDao.foreignKey();
                //关联类型
                type = relationDao.type();
                //操作类型
                //设置可访问
                relationsDaoField.setAccessible(true);
                if (RelationsType.one2one.equals(type)) {
                    //一对一关系
                    //获取关联表的对象
                    Object relationsDaoEntity = relationsDaoField.get(entity);

                    if (relationsDaoEntity != null) {
                        if (relationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                            row += insertAbs(relationsDaoEntity, true);
                        }
                    }
                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //一对多关系
                    //获取关联表的对象
                    List<Object> list = (List<Object>) relationsDaoField.get(entity);
                    if (list != null && list.size() > 0) {
                        for (Object relationsDaoEntity : list) {
                            if (relationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                                row += insertAbs(relationsDaoEntity, true);

                            }
                        }
                    }

                }
            }

        } catch (Exception e) {
            LogUtil.d(this.TAG, "[insert] into DB Exception." + e);
            e.printStackTrace();
            row = -1;
        } finally {
        }
        return row;
    }


    /**
     * 描述：插入列表
     */
    public long insertListAbs(List<T> entityList, boolean flag) {
        long rows = 0;
        try {
            lock.lock();
            String sql = null;
            for (T entity : entityList) {
                ContentValues cv = new ContentValues();
                if (flag) {
                    // id自增
                    sql = setContentValues(entity, cv, TYPE_INCREMENT, METHOD_INSERT);
                } else {
                    // id需指定
                    sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);
                }

                LogUtil.d(TAG, "[insertList]: insert into " + this.mTableName + " " + sql);
                rows += DbFactory.getInstance().getDatabase().insert(this.mTableName, null, cv);


                //获取关联域的操作类型和关系类型
                String foreignKey = null;
                String type = null;
                Field field = null;
                //需要判断是否有关联表
                for (Field RelationsDaoField : allFields) {
                    if (!RelationsDaoField.isAnnotationPresent(RelationDao.class)) {
                        continue;
                    }

                    RelationDao RelationsDao = RelationsDaoField.getAnnotation(RelationDao.class);
                    //获取外键列名
                    foreignKey = RelationsDao.foreignKey();
                    //关联类型
                    type = RelationsDao.type();
                    //设置可访问
                    RelationsDaoField.setAccessible(true);
                    field = RelationsDaoField;
                }

                if (field == null) {
                    continue;
                }


                if (RelationsType.one2one.equals(type)) {
                    //一对一关系
                    //获取关联表的对象
                    T RelationsDaoEntity = (T) field.get(entity);
                    if (RelationsDaoEntity != null) {
                        ContentValues RelationsDaoCv = new ContentValues();
                        if (flag) {
                            // id自增
                            sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_INCREMENT, METHOD_INSERT);
                        } else {
                            // id需指定
                            sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
                        }
                        String RelationsDaoTableName = "";
                        if (RelationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                            Table table = RelationsDaoEntity.getClass().getAnnotation(Table.class);
                            RelationsDaoTableName = table.name();
                        }

                        LogUtil.d(TAG, "[insertList]: insert into " + RelationsDaoTableName + " " + sql);
                        rows += DbFactory.getInstance().getDatabase().insert(RelationsDaoTableName, null, RelationsDaoCv);
                    }

                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //一对多关系
                    //获取关联表的对象
                    List<T> list = (List<T>) field.get(entity);
                    if (list != null && list.size() > 0) {
                        for (T RelationsDaoEntity : list) {
                            ContentValues RelationsDaoCv = new ContentValues();
                            if (flag) {
                                // id自增
                                sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_INCREMENT, METHOD_INSERT);
                            } else {
                                // id需指定
                                sql = setContentValues(RelationsDaoEntity, RelationsDaoCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
                            }
                            String RelationsDaoTableName = "";
                            if (RelationsDaoEntity.getClass().isAnnotationPresent(Table.class)) {
                                Table table = RelationsDaoEntity.getClass().getAnnotation(Table.class);
                                RelationsDaoTableName = table.name();
                            }

                            LogUtil.d(TAG, "[insertList]: insert into " + RelationsDaoTableName + " " + sql);
                            rows += DbFactory.getInstance().getDatabase().insert(RelationsDaoTableName, null, RelationsDaoCv);
                        }
                    }

                }
            }
        } catch (Exception e) {
            LogUtil.d(this.TAG, "[insertList] into DB Exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return rows;
    }

    /**
     * 设置这个ContentValues.
     *
     * @param entity 映射实体
     * @param cv     the cv
     * @param type   id类的类型，是否自增
     * @param method 预执行的操作
     * @return sql的字符串
     * @throws IllegalAccessException the illegal access exception
     */
    private String setContentValues(Object entity, ContentValues cv, int type,
                                    int method) throws IllegalAccessException {
        StringBuffer strField = new StringBuffer("(");
        StringBuffer strValue = new StringBuffer(" values(");
        StringBuffer strUpdate = new StringBuffer(" ");

        // 加载所有字段
        List<Field> allFields = TableHelper.joinFields(entity.getClass().getDeclaredFields(),
                entity.getClass().getSuperclass().getDeclaredFields());
        for (Field field : allFields) {
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);

            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            if (fieldValue == null)
                continue;
            // 处理java.util.Date类型,execSql
            if (Date.class == field.getType()) {
                // 2012-06-10
                cv.put(column.name(), ((Date) fieldValue).getTime());
                continue;
            }
            String value = String.valueOf(fieldValue);
            cv.put(column.name(), value);
            if (method == METHOD_INSERT) {
                strField.append(column.name()).append(",");
                strValue.append("'").append(value).append("',");
            } else {
                strUpdate.append(column.name()).append("=").append("'").append(
                        value).append("',");
            }

        }
        if (method == METHOD_INSERT) {
            strField.deleteCharAt(strField.length() - 1).append(")");
            strValue.deleteCharAt(strValue.length() - 1).append(")");
            LogUtil.i(TAG, "DBImpl: setContentValues: [inerttttttttt]="
                    + strField.toString() + strValue.toString());
            return strField.toString() + strValue.toString();
        } else {
            LogUtil.i(TAG, "DBImpl: setContentValues: [inerttttttttt11]="
                    + strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString());
            return strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString();
        }
    }

}
