package com.db.table;

import com.xbbdb.orm.annotation.Column;
import com.xbbdb.orm.annotation.Id;
import com.xbbdb.orm.annotation.RelationDao;
import com.xbbdb.orm.annotation.RelationsType;
import com.xbbdb.orm.annotation.Table;

/**
 * Created by zhangxiaowei on 18/5/30.
 */
@Table(name = "User")
public class User {
    @Id(name = "userid")
    @Column(name = "userid")
    String userid;
    @Column(name = "name")
    String name = "大秦帝国";
    @Column(name = "phone")
    String phone = "17717886592";
    @Column(name = "alipay2")
    String alipay2 = "5555888888";
    @Column(name = "wx3")
    String wx3 = "66666*****";
    @Column(name = "mitake")
    String mitake = "mitake";
    @Column(name = "code")
    String code = "00700.hk";
    @Column(name = "type")
    String type = "type";
    @Column(name = "useridStaff")
    @RelationDao(name = "userid",foreignKey = "useridStaff",type = RelationsType.one2one)
    Saff useridStaff;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Saff getUseridStaff() {
        return useridStaff;
    }

    public void setUseridStaff(Saff useridStaff) {
        this.useridStaff = useridStaff;
    }

    @Override
    public String toString() {
        return "User{" +
                "userid='" + userid + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", alipay2='" + alipay2 + '\'' +
                ", wx3='" + wx3 + '\'' +
                ", mitake='" + mitake + '\'' +
                ", code='" + code + '\'' +
                ", type='" + type + '\'' +
                ", useridStaff=" + useridStaff +
                '}';
    }

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

    public String getAlipay2() {
        return alipay2;
    }

    public void setAlipay2(String alipay2) {
        this.alipay2 = alipay2;
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
