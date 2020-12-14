package com.db.table;

import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.Table;

/**
 * Created by zhangxiaowei on 18/5/30.
 */
@Table(name = "Saff")
public class Saff {
    @Column(name = "useridStaff")
    String useridStaff;
    @Column(name = "name")
    String name;
    @Column(name = "phone")
    String phone;
    @Column(name = "alipay2")
    String alipay = "1999966";
    @Column(name = "wx3")
    String wx = "88888";
    @Column(name = "mitake")
    String mitake = "mitake";

    public String getMitake() {
        return mitake;
    }

    public void setMitake(String mitake) {
        this.mitake = mitake;
    }

    public String getWx() {
        return wx;
    }

    public void setWx(String wx) {
        this.wx = wx;
    }

    public Saff() {
    }

    public String getAlipay() {
        return alipay;
    }

    public void setAlipay(String alipay) {
        this.alipay = alipay;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Saff(String userid, String name, String phone) {
        this.useridStaff = userid;
        this.name = name;
        this.phone = phone;
    }

    public Saff(String userid, String name) {
        this.useridStaff = userid;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Saff{" +
                "useridStaff='" + useridStaff + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", alipay2='" + alipay + '\'' +
                ", wx3='" + wx + '\'' +
                ", mitake='" + mitake + '\'' +
                '}';
    }

    public String getUserid() {
        return useridStaff;
    }

    public void setUserid(String userid) {
        this.useridStaff = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
