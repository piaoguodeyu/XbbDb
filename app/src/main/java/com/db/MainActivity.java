package com.db;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.db.table.User;
import com.xbbdb.factory.DbFactory;
import com.xbbdb.orm.helper.DbModel;
import com.xbbdb.test.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                DbModel<User> model = DbFactory.getInstance().openSession(User.class);
                List<User> list = new ArrayList<>();
                Log.e("MainActivity", "onCreate= " + list.toString());

                for (int i = 0; i < 13000; i++) {
                    User user = new User();
                    user.setUserid(i + "");
                    list.add(user);
                }
                model.deleteAll();
                long time = System.currentTimeMillis();
                Log.i("MainActivity", "onCreate11= " + time);
                model.insertList(list);
//                model.insert(new User());
                Log.e("MainActivity", "onCreate11= " + (System.currentTimeMillis() - time));
            }
        }).start();
    }
}
