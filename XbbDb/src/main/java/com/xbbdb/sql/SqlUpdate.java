package com.xbbdb.sql;

import android.content.ContentValues;
import android.text.TextUtils;

import com.xbbdb.dao.DbFactory;
import com.xbbdb.orm.AbTableHelper;
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

public class SqlUpdate<T> {
    private final String TAG = SqlUpdate.class.getSimpleName();
    /**
     * The Constant METHOD_INSERT.
     */
    private final int METHOD_INSERT = 0;

    /**
     * The Constant TYPE_NOT_INCREMENT.
     */
    private final int TYPE_NOT_INCREMENT = 0;
    /**
     * The Constant METHOD_UPDATE.
     */
    private final int METHOD_UPDATE = 1;

    /**
     * 锁对象
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 自定义主键
     */

    private String idColumn;


    /**
     * 用一个对象实体初始化这个数据库操作实现类.
     */
    public SqlUpdate(String idColumn
    ) {
        this.idColumn = idColumn;
    }

    /**
     * 获取表名
     *
     * @param daoClasses
     * @return
     */
    private String getTableNeame(Class<?> daoClasses) {
        String tablename = "";
        LogUtil.i(true, TAG, "DBImpl: getTableNeame: [ccccccc]="
                + daoClasses);
        if (daoClasses.isAnnotationPresent(Table.class)) {
            Table table = daoClasses.getAnnotation(Table.class);
            tablename = table.name();
        }
        if (TextUtils.isEmpty(tablename)) {
            LogUtil.i(true, TAG, "DaoConfig: DaoConfig: [daoClasses]="
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
        return AbTableHelper.joinFields(daoClasses.getDeclaredFields(),
                daoClasses.getSuperclass().getDeclaredFields());
    }


    public long update(String column, Object entity) {
        long rows = 0;
        try {

            if (entity == null) {
                return rows;
            }
            List<Field> list = getFiled(entity.getClass());
            String tablename = getTableNeame(entity.getClass());
            ContentValues cv = new ContentValues();
            //注意返回的sql中包含主键列
            String sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_UPDATE);
            String where = column + " = ?";
            String idValues = (String) cv.get(column);
            LogUtil.i(true, TAG, "DBImpl: execSql: [8888888899999]=" + idValues + "  sql= " + sql);

            String[] whereValue = {idValues};

            rows = DbFactory.getInstance().getDatabase().update(tablename, cv, where, whereValue);

            //获取关联域的操作类型和关系类型
            String foreignKey = null;
            String type = null;
            String name = null;
            //需要判断是否有关联表
            for (Field relationsDaoField : list) {
                if (!relationsDaoField.isAnnotationPresent(RelationDao.class)) {
                    continue;
                }

                RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
                //获取外键列名
                foreignKey = relationDao.foreignKey();
                //关联类型
                type = relationDao.type();

                name = relationDao.name();
                //设置可访问
                relationsDaoField.setAccessible(true);
                if (RelationsType.one2one.equals(type)) {
                    update(foreignKey, relationsDaoField.get(entity));
                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //一对多关系
                    //获取关联表的对象
                    List<Object> list1 = (List<Object>) relationsDaoField.get(entity);
                    if (list1 == null || list1.size() == 0) {
                        return rows;
                    }
                    for (Object obj : list1) {
                        rows += update(foreignKey, obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i(true, TAG, "DBImpl: update: [column, entity]="
                    + e);
        } finally {
        }
        return rows;
    }

    /**
     * @param entityList 数据列表,ID主键
     * @return
     */
    public long updateListAbs(List<T> entityList) {
        long rows = -1;
        try {
            lock.lock();
            if (entityList != null && entityList.size() > 0) {
                for (Object data : entityList) {
                    rows += update(this.idColumn, data);
                }
            }
        } catch (Exception e) {
            LogUtil.d(this.TAG, "[execSql] DB Exception.");
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
        List<Field> allFields = AbTableHelper.joinFields(entity.getClass().getDeclaredFields(),
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
            LogUtil.i(true, TAG, "DBImpl: setContentValues: [inerttttttttt]="
                    + strField.toString() + strValue.toString());
            return strField.toString() + strValue.toString();
        } else {
            LogUtil.i(true, TAG, "DBImpl: setContentValues: [inerttttttttt11]="
                    + strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString());
            return strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString();
        }
    }

}
