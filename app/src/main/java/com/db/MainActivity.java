package com.db;

import android.app.Activity;
import android.os.Bundle;

import com.db.table.User;
import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.helper.DbModel;
import com.xbbdb.test.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DbModel<User> model = DbFactory.getInstance().openSession(User.class);
        model.insert(new User("100","xiaowei"));
    }
}
