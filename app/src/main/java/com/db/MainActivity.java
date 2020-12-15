package com.db;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.db.table.Saff;
import com.db.table.User;
import com.db.util.ThreadPoolManager;
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


        ThreadPoolManager.submit(new Runnable() {
            @Override
            public void run() {
                insert();
            }
        });
    }

    private void insert() {
        DbModel<User> model = DbFactory.getInstance().openSession(User.class);


        DbModel<Saff> modelSaff = DbFactory.getInstance().openSession(Saff.class);
        Log.d("MainActivity123", "onCreate= " + modelSaff.queryList());

        List<User> list = new ArrayList<>();
        List<User> users = model.queryList();
        Log.e("MainActivity123", "onCreate= " + users);
//        model.deleteAll();
//        modelSaff.deleteAll();
//                for (int i = 0; i < 200; i++) {
//                    User user = new User();
//                    user.setUserid(i + "");
//                    Saff saff=new Saff();
//                    saff.setUserid(i + "");
//                    user.setUseridStaff(saff);
//                    list.add(user);
//
//                }
        for (int i = 1; i < 200; i++) {
            long time = System.currentTimeMillis();
            User user = new User();
            user.setUserid(i + "");
            user.setName("00700.hk");
            user.setAlipay2("00700.hk");
            user.setWx3("00700.hk");
            user.setMitake("00700.hk");
            user.setPhone("00700.hk");
            Saff saff = new Saff();
            saff.setUserid(i+"");
            saff.setAlipay("00700.hk");
            saff.setMitake("00700.hk");
            saff.setName("00700.hk");
            user.setUseridStaff(saff);
            Log.i("MainActivity", "onCreate11= " + time);
//            model.updateByColumn("userid", user);
            list.add(user);
        }
        model.insertList(list);
//                Log.d("MainActivity1", "user= " + model.queryList());
//                Log.d("MainActivity1", "modelSaff= " + modelSaff.queryList());
//                Log.e("MainActivity1", "onCreate11= " + (System.currentTimeMillis() - time));
    }
}
