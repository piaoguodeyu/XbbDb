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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.Table;
import com.xbbdb.utils.LogUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author zhangxiaowei
 * @version v3.0
 */
public class DBHelper extends SQLiteOpenHelper {
    private final String TAG = DBHelper.class.getSimpleName();
    /**
     * The model classes.
     */
    private Class<?>[] modelClasses;

    /**
     * 初始化一个AbSDDBHelper.
     *
     * @param context      应用context
     * @param name         数据库名
     * @param factory      数据库查询的游标工厂
     * @param version      数据库的新版本号
     * @param modelClasses 要初始化的表的对象
     */
    public DBHelper(Context context, String name, CursorFactory factory,
                    int version, Class<?>[] modelClasses) {
        super(context, name, factory, version);
        this.modelClasses = modelClasses;
    }

    /**
     * 描述：表的创建.
     *
     * @param db 数据库对象
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        DbFactory.getInstance().onCreateWriteDatabase(db);
        TableHelper.createTablesByClasses(db, this.modelClasses);
    }

    /**
     * 描述：表的重建.
     *
     * @param db         数据库对象
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     *                   int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.e(TAG, "DbFactory: onUpgrade: [uuuuuuu]="
                + "  oldVersion=" + oldVersion + "  newVersion" + newVersion);
        try {
            Map<String, Class<?>> hashMap = new HashMap<>();
            for (Class<?> clazz : modelClasses) {
                String tableNeame = getTableNeame(clazz);
                hashMap.put(tableNeame, clazz);
            }
            DbFactory.getInstance().onCreateWriteDatabase(db);
//            DbFactory.getInstance().openWriteDatabase();
            saveOldTables(db, modelClasses, hashMap);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            DbFactory.getInstance().closeWriteDatabase();
        }
        Log.i("DBHelper", "onUpgradeuuuuuuu888888= ");
    }

    private String getTableNeame(Class<?> daoClasses) {
        String tablename = "";
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

    private List<String> saveOldTables(SQLiteDatabase db, Class<?>[] daoClasses, Map<String, Class<?>> hashMap) {
        List<String> list = new ArrayList<>();
        try {
            if (daoClasses == null || daoClasses.length == 0) {
                return list;
            }
            Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
            while (cursor.moveToNext()) {
                //遍历出表名
                String tablename = cursor.getString(0);
                Class claColum = hashMap.get(tablename);
                if (claColum == null) {
                    db.execSQL("DROP TABLE IF EXISTS " + tablename);
                } else {
                    hashMap.remove(tablename);
                    List<String> temp = getColumns(db, tablename);
                    List<String> listColum = getColumns(db, tablename);
                    List<String> listColumClazz = getColumns(claColum);
                    temp.addAll(listColumClazz);
                    listColumClazz.removeAll(listColum);
                    if (!listColumClazz.isEmpty()) {
                        for (String colum : listColumClazz) {
                            StringBuilder builder = new StringBuilder("alter table ");
                            builder.append(tablename).append(" add column");
                            builder.append(" ").append(colum).append(" TEXT");
                            db.execSQL(builder.toString());
                        }
                    }
                    listColum.removeAll(temp);
                    if (!listColum.isEmpty()) {
                        for (String colum : listColum) {
                            StringBuilder builder = new StringBuilder("alter table ");
                            builder.append(tablename).append(" drop column");
                            builder.append(" ").append(colum);
                            db.execSQL(builder.toString());
                        }
                    }

                }

            }
            List<Class> listtable = new ArrayList();
            for (Map.Entry<String, Class<?>> enty : hashMap.entrySet()) {
                listtable.add(enty.getValue());
            }
            modelClasses = listtable.toArray(new Class[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private List<String> getColumns(Class daoClasses) {
        List<String> list = new ArrayList<>();
        List<Field> allFields = TableHelper.joinFieldsOnlyColumn(daoClasses.getDeclaredFields(), daoClasses.getSuperclass().getDeclaredFields());
        if (allFields == null) {
            return list;
        }
        for (int i = 0; i < allFields.size(); i++) {
            Field field = allFields.get(i);
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            String name = column.name();
            list.add(name);
        }
        return list;
    }

    private List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 1", null);
            if (cursor != null) {
                columns = new ArrayList<>(Arrays.asList(cursor.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return columns;
    }
}
