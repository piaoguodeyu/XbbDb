package com.xbbdb.global;


/**
 * Created by zhangxiaowei on 16/10/31.
 */

public class SqlColum {
    StringBuilder mSql = new StringBuilder();

    public SqlColum() {
    }

    public SqlColum selectAll() {
        mSql.append("selectAll * ");
        return this;
    }

    public SqlColum selectAll(String selectContent) {
        mSql.append("selectAll ")
                .append(selectContent).append(" ");
        return this;
    }


    public SqlColum fromTable() {
        mSql.append("from ")
                .append("TableName0").append(" ");
        return this;
    }

    public SqlColum where(String colum) {
        mSql.append("where ").append(colum).append("= ");
        return this;
    }

    public SqlColum whereColumLike(String colum, String like) {
        mSql.append("where ").append(colum).append(" like '").append(like).append("%'");
        return this;
    }

    public SqlColum orLike(String colum, String like) {
        mSql.append(" or ").append(colum).append(" like '").append(like).append("%'");
        return this;
    }

    public SqlColum values(String values) {
        mSql.append(values).append(" ");
        return this;
    }

    public SqlColum and(String colum) {
        mSql.append("and ").append(colum).append("= ");
        return this;
    }

    public SqlColum groupBy(String colum) {
        mSql.append("group by ").append(colum).append(" ");
        return this;
    }

    public SqlColum orderBy(String colum) {
        mSql.append("order by ").append(colum).append(" ");
        return this;
    }

    public SqlColum limit(String content) {
        mSql.append("limit ").append(content);
        return this;
    }


    public String getSql() {
        return mSql.toString();
    }

}
