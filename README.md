# XbbDb
安卓数据库框架
数据库注册：最好在自定义的Application里

    public class MyApp extends Application {
    private Class<?>[] clazz = {User.class};
    @Override
    public void onCreate() {
        super.onCreate();
        DbConfig config = new DbConfig();
        config.setContext(getApplicationContext())
                .setDBNAME("test.db").setDBVERSION(1)
                .setClazz(clazz);
        DbFactory.init(config);
    }
    }
    
    2: 增删改查
    DbModel<User> dbModel=DbFactory.getInstance().openSession(User.class);
        dbModel.queryList();
        dbModel.deleteOne(new User());
        dbModel.update(new User());
        dbModel.insert(new User());
    
    
    
User

     @Table(name = "User")
     public class User {
       @Column(name = "userid")
       String userid = "60000";
       
        public String getUserid() {
        return userid;
        }

        public void setUserid(String userid) {
        this.userid = userid;
       }

     }
