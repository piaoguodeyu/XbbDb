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
package com.xbbdb.orm;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.Id;
import com.xbbdb.orm.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author zhangxiaowei
 */
public class TableHelper {

    /**
     * 日志标记.
     */
    private static final String TAG = "TableHelper";

    /**
     * 根据映射的对象创建表.
     *
     * @param <T>    the generic type
     * @param clazzs 对象映射
     */
    public static <T> void createTablesByClasses(Class<?>[] clazzs) {
        for (Class<?> clazz : clazzs) {
            createTable(clazz);
        }
    }

    /**
     * 根据映射的对象删除表.
     *
     * @param <T>    the generic type
     * @param db     数据库对象
     * @param clazzs 对象映射
     */
    public static <T> void dropTablesByClasses(SQLiteDatabase db, Class<?>[] clazzs) {
        for (Class<?> clazz : clazzs) {
            dropTable(db, clazz);
        }
    }

    /**
     * 创建表.
     *
     * @param <T>   the generic type
     * @param clazz 对象映射
     */
    public static <T> void createTable(Class<T> clazz) {
        String tableName = "";
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            tableName = table.name();
        }
        if (TextUtils.isEmpty(tableName)) {
            Log.d(TAG, "想要映射的实体[" + clazz.getName() + "],未注解@Table(name=\"?\"),被跳过");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(tableName).append(" (");

        List<Field> allFields = TableHelper.joinFieldsOnlyColumn(clazz);
        for (Field field : allFields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                Id column = field.getAnnotation(Id.class);
                sb.append(column.name() + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
                continue;
            }
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);

            String columnType = "";
            columnType = column.type();
            sb.append(column.name() + " " + columnType);

            if (column.length() != 0) {
                sb.append("(" + column.length() + ")");
            }
            //实体类定义为Integer类型后不能生成Id异常
//			if ((field.isAnnotationPresent(Id.class))
//					&& ((field.getType() == Integer.TYPE) || (field.getType() == Integer.class)))
//				sb.append(" primary key autoincrement");
//			else if (field.isAnnotationPresent(Id.class)) {
//				sb.append(" primary key");
//			}

            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length() - 1);
        sb.append(")");
        String sql = sb.toString();
        Log.d(TAG, "crate table [" + tableName + "]: " + sql);
        DbFactory.getInstance().getWriteDatabase().execSQL(sql);
    }

    /**
     * 删除表.
     *
     * @param <T>   the generic type
     * @param db    根据映射的对象创建表.
     * @param clazz 对象映射
     */
    public static <T> void dropTable(SQLiteDatabase db, Class<T> clazz) {
        String tableName = "";
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            tableName = table.name();
        }
        String sql = "DROP TABLE IF EXISTS " + tableName;
        Log.d(TAG, "dropTable[" + tableName + "]:" + sql);
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 合并Field数组并去重,并实现过滤掉非Column字段,和实现Id放在首字段位置功能.
     *
     * @return 属性的列表
     */
    public static List<Field> joinFieldsOnlyColumn(Class<?> tempClass) {
        List<Field> fieldList = new ArrayList<>();
        while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
        }
        List<Field> list = new ArrayList<Field>();
//        Map<String, Field> map = new LinkedHashMap<String, Field>();
        for (Field field : fieldList) {
            if (field.isAnnotationPresent(Id.class)) {
//                Id column = field.getAnnotation(Id.class);
                list.add(0, field);
                continue;
            }
            // 过滤掉非Column定义的字段
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
//            Column column = field.getAnnotation(Column.class);
            list.add(field);
        }
//        for (String key : map.keySet()) {
//            Field tempField = map.get(key);
//            // 如果是Id则放在首位置.
//            if (tempField.isAnnotationPresent(Id.class)) {
//                list.add(0, tempField);
//            } else {
//                list.add(tempField);
//            }
//        }
        return list;
    }
}
