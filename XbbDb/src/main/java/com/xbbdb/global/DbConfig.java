package com.xbbdb.global;

/**
 * Created by zhangxiaowei on 18/5/30.
 */

public class DbConfig {
    /**
     * 数据库名
     */
    private String DBNAME;
    /**
     * 当前数据库的版本
     */
    private int DBVERSION;
    /**
     * 要初始化的表
     */
    private Class<?>[] mClazz;

    private DbConfig() {
    }

    private DbConfig(DbConfigBuilder builder) {
        this.DBNAME = builder.DBNAME;
        this.DBVERSION = builder.DBVERSION;
        this.mClazz = builder.mClazz;
    }

    public static class DbConfigBuilder {
        /**
         * 数据库名
         */
        private String DBNAME;
        /**
         * 当前数据库的版本
         */
        private int DBVERSION;
        /**
         * 要初始化的表
         */
        private Class<?>[] mClazz;

        public DbConfigBuilder setDBNAME(String DBNAME) {
            this.DBNAME = DBNAME;
            return this;
        }

        public DbConfigBuilder setDBVERSION(int DBVERSION) {
            this.DBVERSION = DBVERSION;
            return this;
        }

        public DbConfigBuilder setClazz(Class<?>[] mClazz) {
            this.mClazz = mClazz;
            return this;
        }

        public DbConfig builder() {
            return new DbConfig(this);
        }
    }
}
