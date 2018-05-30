package com.db.model;

import com.db.table.User;
import com.xbbdb.orm.helper.DbModel;

/**
 * Created by zhangxiaowei on 18/5/30.
 */

public class UserModel extends DbModel<User> {
    public UserModel(Class<User> clazz) {
        super(clazz);
    }
}
