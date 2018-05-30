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
    @Column(name = "phone")
    String phone;
    @Column(name = "alipay1")
    String alipay1 = "00988888888";
    @Column(name = "wx3")
    String wx3 = "66666";
    @Column(name = "mitake")
    String mitake = "mitake";

    public String getMitake() {
        return mitake;
    }

    public void setMitake(String mitake) {
        this.mitake = mitake;
    }

    public String getWx3() {
        return wx3;
    }

    public void setWx3(String wx3) {
        this.wx3 = wx3;
    }

    public User() {
    }

    public String getAlipay1() {
        return alipay1;
    }

    public void setAlipay1(String alipay1) {
        this.alipay1 = alipay1;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User(String userid, String name, String phone) {
        this.userid = userid;
        this.name = name;
        this.phone = phone;
    }

    public User(String userid, String name) {
        this.userid = userid;
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "userid='" + userid + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", alipay1='" + alipay1 + '\'' +
                ", wx3='" + wx3 + '\'' +
                ", mitake='" + mitake + '\'' +
                '}';
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
