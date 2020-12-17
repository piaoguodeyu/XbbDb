package com.xbbdb.sql;

import android.content.ContentValues;
import android.text.TextUtils;

import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.TableHelper;
import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.Id;
import com.xbbdb.orm.annotation.RelationDao;
import com.xbbdb.orm.annotation.RelationsType;
import com.xbbdb.orm.annotation.Table;
import com.xbbdb.utils.XbbLogUtil;

import java.lang.reflect.Field;
import java.util.List;

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
        XbbLogUtil.i(TAG, "DBImpl: getTableNeame: [ccccccc]="
                + daoClasses);
        daoClasses = TableHelper.getTableClass(daoClasses);
        if (daoClasses.isAnnotationPresent(Table.class)) {
            Table table = daoClasses.getAnnotation(Table.class);
            tablename = table.name();
        }
        if (TextUtils.isEmpty(tablename)) {
            XbbLogUtil.i(TAG, "DaoConfig: DaoConfig: [daoClasses]="
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
        return TableHelper.joinFieldsOnlyColumn(daoClasses);
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
        long row = 0L;
        try {
            ContentValues cv = new ContentValues();
            // 加载所有字段
            List<Field> allFields = TableHelper.joinFieldsOnlyColumn(entity.getClass());
            Field relationsDaoField = setContentValues(entity, cv, allFields);
            String tablename = getTableNeame(entity.getClass());
            row = DbFactory.getInstance().getWriteDatabase().insert(tablename, null, cv);

            //获取关联域的操作类型和关系类型
            String type = null;
            //需要判断是否有关联表
            if (relationsDaoField == null) {
                return row;
            }
            RelationDao relationDao = relationsDaoField.getAnnotation(RelationDao.class);
            //获取外键列名
            type = relationDao.type();
            //操作类型
            //设置可访问
            relationsDaoField.setAccessible(true);
            if (RelationsType.one2one.equals(type)) {
                //一对一关系
                //获取关联表的对象
                Object relationsDaoEntity = relationsDaoField.get(entity);

                if (relationsDaoEntity != null) {
                    Class daoClasses = relationsDaoEntity.getClass();
                    daoClasses = TableHelper.getTableClass(daoClasses);
                    if (daoClasses.isAnnotationPresent(Table.class)) {
                        row = insertAbs(relationsDaoEntity, true);
                    }
                }
            } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                //一对多关系
                //获取关联表的对象
                List<Object> list = (List<Object>) relationsDaoField.get(entity);
                if (list != null && list.size() > 0) {
                    for (Object relationsDaoEntity : list) {
                        Class clazz = relationsDaoEntity.getClass();
                        clazz = TableHelper.getTableClass(clazz);
                        if (clazz.isAnnotationPresent(Table.class)) {
                            row = insertAbs(relationsDaoEntity, true);

                        }
                    }
                }

            }
//            }

        } catch (Exception e) {
            XbbLogUtil.d(this.TAG, "[insert] into DB Exception." + e);
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
            // 加载所有字段
            /**
             * 关联表的 列
             */
            Field relationfield = null;
            String relationtype = null;

            List<Field> fieldList = null;
            for (T entity : entityList) {
                ContentValues cv = new ContentValues();
                relationfield = setContentValues(entity, cv, allFields);
//                LogUtil.d(TAG, "[insertList]: insert into " + this.mTableName + " " + sql);
                rows += DbFactory.getInstance().getWriteDatabase().insert(this.mTableName, null, cv);

                if (relationfield != null && relationtype == null) {
                    //需要判断是否有关联表
                    RelationDao relationDao = relationfield.getAnnotation(RelationDao.class);
                    //获取外键列名
//                    foreignKey = RelationsDao.foreignKey();
                    //关联类型
                    relationtype = relationDao.type();
                    //设置可访问
                    relationfield.setAccessible(true);
                } else {
                    continue;
                }
                //获取关联域的操作类型和关系类型
//                String foreignKey = null;
                if (RelationsType.one2one.equals(relationtype)) {
                    //一对一关系
                    //获取关联表的对象
                    Object relationsDaoEntity = relationfield.get(entity);
                    if (relationsDaoEntity != null) {
                        ContentValues relationsDaoCv = new ContentValues();
                        if (fieldList == null) {
                            fieldList = TableHelper.joinFieldsOnlyColumn(relationsDaoEntity.getClass());

                        }
                        setContentValues(relationsDaoEntity, relationsDaoCv, fieldList);
                        String relationsDaoTableName = "";
                        Class clazz = relationsDaoEntity.getClass();
                        clazz = TableHelper.getTableClass(clazz);
                        if (clazz.isAnnotationPresent(Table.class)) {
                            Table table = TableHelper.getTable(clazz);
                            relationsDaoTableName = table.name();
                        }

//                        LogUtil.d(TAG, "[insertList]: insert into " + RelationsDaoTableName + " " + sql);
                        rows += DbFactory.getInstance().getWriteDatabase().insert(relationsDaoTableName, null, relationsDaoCv);
                    }

                } else if (RelationsType.one2many.equals(relationtype) || RelationsType.many2many.equals(relationtype)) {
                    //一对多关系
                    //获取关联表的对象
                    List<Object> list = (List<Object>) relationfield.get(entity);
                    if (list != null && list.size() > 0) {
                        for (Object relationsDaoEntity : list) {
                            ContentValues contentValues = new ContentValues();
                            if (fieldList == null) {
                                fieldList = TableHelper.joinFieldsOnlyColumn(relationsDaoEntity.getClass());

                            }
                            setContentValues(relationsDaoEntity, contentValues, fieldList);
                            String relationsDaoTableName = "";
                            Class daoClasses = relationsDaoEntity.getClass();
                            daoClasses = TableHelper.getTableClass(daoClasses);
                            if (daoClasses.isAnnotationPresent(Table.class)) {
                                Table table = TableHelper.getTable(daoClasses);
                                relationsDaoTableName = table.name();
                            }

//                            LogUtil.d(TAG, "[insertList]: insert into " + relationsDaoTableName + " " + sql);
                            rows += DbFactory.getInstance().getWriteDatabase().insert(relationsDaoTableName, null, contentValues);
                        }
                    }

                }
            }
        } catch (Exception e) {
            XbbLogUtil.d(this.TAG, "[insertList] into DB Exception." + e);
            e.printStackTrace();
        } finally {
        }

        return rows;
    }

    /**
     * 设置这个ContentValues.
     *
     * @param entity        映射实体
     * @param contentValues the cv
     * @return sql的字符串
     * @throws IllegalAccessException the illegal access exception
     */
    private Field setContentValues(Object entity, ContentValues contentValues, List<Field> allFields
    ) throws IllegalAccessException {
        Field fieldRelationDao = null;
        for (Field field : allFields) {
            if (field.isAnnotationPresent(Id.class)) {
                continue;
            }
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);

            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            if (fieldValue == null)
                continue;
            RelationDao columnRelationDao = field.getAnnotation(RelationDao.class);
            if (columnRelationDao != null) {
                fieldRelationDao = field;
                continue;
            }
            String value = String.valueOf(fieldValue);
            contentValues.put(column.name(), value);
        }
        return fieldRelationDao;
    }

}
