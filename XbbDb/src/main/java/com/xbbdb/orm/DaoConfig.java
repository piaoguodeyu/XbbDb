package com.xbbdb.orm;

import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.Id;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by zhangxiaowei on 16/10/31.
 */

public class DaoConfig {
    public String mTablename = "";
    ColumnName[] properties;

    public DaoConfig(Class<?> daoClasses, String tablename) {
        mTablename = tablename;

        List<Field> allFields = AbTableHelper.joinFieldsOnlyColumn(daoClasses.getDeclaredFields(), daoClasses.getSuperclass().getDeclaredFields());
        if (allFields == null) {
            return;
        }
        for (int i = 0; i < allFields.size(); i++) {
            Field field = allFields.get(i);
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }

            Column column = field.getAnnotation(Column.class);

            String columnType = column.type();
            boolean primary = false;
            if (field.isAnnotationPresent(Id.class)) {
                primary = true;
            }
            ColumnName name = new ColumnName(columnType, column.name(), primary);
            properties[i] = name;
        }
    }

    /**
     * 获取列类型.
     *
     * @param fieldType the field type
     * @return 列类型
     */
//    private String getColumnType(Class<?> fieldType) {
//        if (String.class == fieldType) {
//            return "TEXT";
//        }
//        if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {
//            return "INTEGER";
//        }
//        if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
//            return "BIGINT";
//        }
//        if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
//            return "FLOAT";
//        }
//        if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
//            return "INT";
//        }
//        if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {
//            return "DOUBLE";
//        }
//        if (Blob.class == fieldType) {
//            return "BLOB";
//        }
//
//        return "TEXT";
//    }
}
