package com.db;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.db.table.User;
import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.helper.DbModel;
import com.xbbdb.test.R;

import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DbModel<User> model = DbFactory.getInstance().openSession(User.class);
        List<User> list=model.queryList();
        Log.e("MainActivity", "onCreate= "+list.toString());
        model.insert(new User("88888","xiaowei8888","0000700"));
    }
}
