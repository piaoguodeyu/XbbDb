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
import android.util.Log;

import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.ColumnIndex;
import com.xbbdb.orm.annotation.Id;
import com.xbbdb.orm.annotation.Table;
import com.xbbdb.utils.XbbLogUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author zhangxiaowei
 * @version v3.0
 */
public class DBHelper extends SQLiteOpenHelper {
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
        DbFactory.getInstance().openWriteDatabase();
        TableHelper.createTablesByClasses(this.modelClasses);
        DbFactory.getInstance().closeWriteDatabase();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
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
        daoClasses = TableHelper.getTableClass(daoClasses);
        if (daoClasses.isAnnotationPresent(Table.class)) {
            Table table = daoClasses.getAnnotation(Table.class);
            tablename = table.name();
        }
        return tablename;
    }

    private List<String> saveOldTables(SQLiteDatabase db, Class<?>[] daoClasses, Map<String, Class<?>> hashMap) {
        List<String> list = new ArrayList<>();
        try {
            if (daoClasses == null || daoClasses.length == 0) {
                return list;
            }


            Cursor cursorsqlite_master = db.rawQuery("select * from sqlite_master", null);
            while (cursorsqlite_master.moveToNext()) {
                String[] col = cursorsqlite_master.getColumnNames();

                try {
                    for (int i = 0; i < col.length; i++) {
                        String data = col[i];
                        XbbLogUtil.i("saveOldTables", "data= " + data + " name= " +
                                cursorsqlite_master.getColumnIndex(data)
                                + " value= " + cursorsqlite_master.getString(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
            while (cursor.moveToNext()) {
                //遍历出表名
                String tablename = cursor.getString(0);
                Class claColum = hashMap.get(tablename);
                XbbLogUtil.e("saveOldTables", "tablename= " + tablename + " claColum= " + claColum);
                if (claColum == null) {
                    if ("android_metadata".equals(tablename) || "sqlite_sequence".equals(tablename)) {
                        continue;
                    }
                    /**
                     * 说明该表被删了
                     */
                    try {
                        db.execSQL("DROP TABLE IF EXISTS " + tablename);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    hashMap.remove(tablename);
                    //查找当前表的列
                    CopyOnWriteArrayList<String> listColum = getColumns(db, tablename);
                    //获取新版本该表对应的字段
                    CopyOnWriteArrayList<String> listColumClazz = getColumns(claColum);
                    //获取新版本该表对应的字段
                    CopyOnWriteArrayList<String> newColumClazz = getColumns(claColum);
                    //取出该表新增字段
                    listColumClazz.removeAll(listColum);
                    if (!listColumClazz.isEmpty()) {//添加新字段
                        for (String colum : listColumClazz) {
                            StringBuilder builder = new StringBuilder("alter table ");
                            builder.append(tablename).append(" add column");
                            builder.append(" ").append(colum).append(" TEXT");
                            try {
                                db.execSQL(builder.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //去重新表对应字段，移出不需要的表字段
                    listColum.removeAll(newColumClazz);
                    if (!listColum.isEmpty()) {
                        for (String colum : listColum) {
                            StringBuilder builder = new StringBuilder("alter table ");
                            builder.append(tablename).append(" drop column");
                            builder.append(" ").append(colum);
                            try {
                                db.execSQL(builder.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    List<Field> indexList = TableHelper.getIndexList(claColum, null);
                    //增加索引
                    if (!indexList.isEmpty()) {
                        for (Field field : indexList) {
                            try {
                                //先删除索引  再建索引
                                DbFactory.getInstance().getWriteDatabase().execSQL("DROP INDEX " + field.getName() + "_index");
                                ColumnIndex columnIndex = field.getAnnotation(ColumnIndex.class);
                                String indexSql = "CREATE INDEX " + field.getName() + "_index" + " ON " + tablename + " (" + columnIndex.value() + ")";
                                DbFactory.getInstance().getWriteDatabase().execSQL(indexSql);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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

    private CopyOnWriteArrayList<String> getColumns(Class daoClasses) {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        List<Field> allFields = TableHelper.joinFieldsOnlyColumn(daoClasses);
        if (allFields == null) {
            return list;
        }
        for (int i = 0; i < allFields.size(); i++) {
            Field field = allFields.get(i);
            if (field.isAnnotationPresent(Id.class)) {
                Id column = field.getAnnotation(Id.class);
                list.add(column.name());
                continue;
            }
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            String name = column.name();
            list.add(name);
        }
        return list;
    }

    private CopyOnWriteArrayList<String> getColumns(SQLiteDatabase db, String tableName) {
        CopyOnWriteArrayList<String> columns = new CopyOnWriteArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 1", null);
            if (cursor != null) {
                columns = new CopyOnWriteArrayList<>(Arrays.asList(cursor.getColumnNames()));
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
