package com.sun.account;

import com.sun.utils.SharedPreferencesUtil;
import com.sun.utils.Utils;


/**
 * Created by guoyao on 2016/12/13.
 */
public class Account {
    public static final Info Client = new Info("apple", "1f3870be274f6c49b3e31a0c6728957f", 2);
    public static final Info Server = new Info("鬼魇", "1a1fa8b08f04fa4d85313986e6f6d288", 1);

    public static final String KEY_LONG_LOGIN_HISTORY = "login_time";
    public static final String KEY_INT_LOGIN_USER = "login_user";
    public static final long LoginDuration = 60 * 60 * 1000;

    private Info login;

    public static class Info{
        public Info(String name, String password, int id){
            this.name = name;
            this.password = password;
            this.id = id;
        }
        private String name;
        private String password;
        private int id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public Account(){}

    public boolean Login(String password){
        String md5 = Utils.md5(password.toLowerCase());
        if(Client.password.equals(md5)){
            SharedPreferencesUtil.putLong(KEY_LONG_LOGIN_HISTORY, System.currentTimeMillis());
            SharedPreferencesUtil.putInt(KEY_INT_LOGIN_USER, Client.id);
            login = Client;
            return true;
        }else if(Server.password.equals(md5)){
            SharedPreferencesUtil.putLong(KEY_LONG_LOGIN_HISTORY, System.currentTimeMillis());
            SharedPreferencesUtil.putInt(KEY_INT_LOGIN_USER, Server.id);
            login = Server;
            return true;
        }
        return false;
    }

    public boolean isLogin(){
        if(login != null){
            return true;
        }
        long time = SharedPreferencesUtil.getLong(KEY_LONG_LOGIN_HISTORY);
        if(time > 0 && System.currentTimeMillis() - time < LoginDuration){
            int id = SharedPreferencesUtil.getInt(KEY_INT_LOGIN_USER);
            if(id == Client.id){
                login = Client;
            }else if (id == Server.id){
                login = Server;
            }
        }
        return login != null;
    }

    public String getLoginName(){
        return login != null ? login.name : "null";
    }

    public int getLoginId(){
        return login != null ? login.id : 0;
    }

    public boolean isLoginAccount(int id){
        return login != null && login.id == id;
    }
}
