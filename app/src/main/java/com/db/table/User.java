package com.db.table;

import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.Table;

/**
 * Created by zhangxiaowei on 18/5/30.
 */
@Table(name = "User")
public class User {
    @Column(name = "userid")
    String userid;
    @Column(name = "name")
    String name;

    public User(String userid, String name) {
        this.userid = userid;
        this.name = name;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
