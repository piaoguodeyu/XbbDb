package com.xbbdb.sql;

import android.content.ContentValues;
import android.text.TextUtils;

import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.TableHelper;
import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.RelationDao;
import com.xbbdb.orm.annotation.RelationsType;
import com.xbbdb.orm.annotation.Table;
import com.xbbdb.utils.XbbLogUtil;

import java.lang.reflect.Field;
import java.util.List;

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


    public long update(String[] colusmns, Object entity, List<Field> list, String tablename) {
        long rows = 0;
        try {

            if (entity == null) {
                return rows;
            }
//            List<Field> list = getFiled(entity.getClass());
//            String tablename = getTableNeame(entity.getClass());
            ContentValues cv = new ContentValues();
            //注意返回的sql中包含主键列
            setContentValues(entity, cv, list);
            StringBuilder whereBuild = new StringBuilder();
            String[] whereValue = new String[colusmns.length];
            for (int i = 0; i < colusmns.length; i++) {
                String column = colusmns[i];
                if (i > 0) {
                    whereBuild.append(" and ").append(column).append(" =?");
                } else {
                    whereBuild.append(column).append(" =?");
                }
                whereValue[i] = (String) cv.get(column);
            }
            if (XbbLogUtil.isDebug()) {
                String sql = "update " + tablename + " set " + cv + " where " + whereBuild.toString();
                XbbLogUtil.i(TAG, "DBImpl: sql= " + sql);
            }
            rows = DbFactory.getInstance().getWriteDatabase().update(tablename, cv, whereBuild.toString(), whereValue);
            //获取关联域的操作类型和关系类型
            String foreignKey = null;
            String type = null;
//            String name = null;
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

//                name = relationDao.name();
                //设置可访问
                relationsDaoField.setAccessible(true);
                if (RelationsType.one2one.equals(type)) {
                    /**
                     * 获取关联表实体类
                     */
                    Object relationsObj = relationsDaoField.get(entity);
                    if (relationsObj == null) {
                        continue;
                    }
                    List<Field> fieldList = getFiled(relationsObj.getClass());
                    String tbname = getTableNeame(relationsObj.getClass());
                    update(new String[]{foreignKey}, relationsObj, fieldList, tbname);
                } else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
                    //一对多关系
                    //获取关联表的对象
                    List<Object> list1 = (List<Object>) relationsDaoField.get(entity);
                    if (list1 == null || list1.size() == 0) {
                        return rows;
                    }
                    List<Field> fieldList = null;
                    String tbname = null;
                    for (Object obj : list1) {
                        if (fieldList == null) {
                            fieldList = getFiled(obj.getClass());
                            tbname = getTableNeame(obj.getClass());
                        }
                        rows += update(new String[]{foreignKey}, obj, fieldList, tbname);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            XbbLogUtil.i(TAG, "DBImpl: update: [column, entity]="
                    + e);
        } finally {
        }
        return rows;
    }

    /**
     * @param entityList 数据列表,ID主键
     * @return
     */
    public long updateListAbs(List<T> entityList, List<Field> list, String tablename) {
        long rows = -1;
        try {
            if (entityList != null && entityList.size() > 0) {
                for (Object data : entityList) {
                    rows += update(new String[]{this.idColumn}, data, list, tablename);
                }
            }
        } catch (Exception e) {
            XbbLogUtil.d(this.TAG, "[execSql] DB Exception.");
            e.printStackTrace();
        }

        return rows;
    }

    /**
     * 设置这个ContentValues.
     *
     * @param entity 映射实体
     * @param cv     the cv
     * @return sql的字符串
     * @throws IllegalAccessException the illegal access exception
     */
    private String setContentValues(Object entity, ContentValues cv, List<Field> allFields) throws IllegalAccessException {
//        StringBuffer strField = new StringBuffer("(");
//        StringBuffer strValue = new StringBuffer(" values(");
//        StringBuffer strUpdate = new StringBuffer(" ");

        // 加载所有字段
//        List<Field> allFields = TableHelper.joinFields(entity.getClass().getDeclaredFields(),
//                entity.getClass().getSuperclass().getDeclaredFields());
        for (Field field : allFields) {
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);

            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            if (fieldValue == null)
                continue;
            String value = String.valueOf(fieldValue);
            cv.put(column.name(), value);
//            if (method == METHOD_INSERT) {
//                strField.append(column.name()).append(",");
//                strValue.append("'").append(value).append("',");
//            } else {
//                strUpdate.append(column.name()).append("=").append("'").append(
//                        value).append("',");
//            }

        }
//        if (method == METHOD_INSERT) {
//            strField.deleteCharAt(strField.length() - 1).append(")");
//            strValue.deleteCharAt(strValue.length() - 1).append(")");
//            LogUtil.i(TAG, "DBImpl: setContentValues: [inerttttttttt]="
//                    + strField.toString() + strValue.toString());
//            return strField.toString() + strValue.toString();
//        } else {
//            LogUtil.i(TAG, "DBImpl: setContentValues: [inerttttttttt11]="
//                    + strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString());
//            return strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString();
//        }
        return "";
    }

}
