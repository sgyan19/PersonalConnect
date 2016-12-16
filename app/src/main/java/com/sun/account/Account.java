package com.sun.account;

import com.sun.utils.Utils;

/**
 * Created by guoyao on 2016/12/13.
 */
public class Account {
    public static final Info Client = new Info("apple", "1f3870be274f6c49b3e31a0c6728957f", 2);
    public static final Info Server = new Info("鬼魇", "1f3870be274f6c49b3e31a0c6728957f", 1);

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
            login = Client;
            return true;
        }else if(Server.password.equals(md5)){
            login = Server;
            return true;
        }
        return false;
    }

    public boolean isLogin(){
        return login != null;
    }

    public String getLoginName(){
        return login != null ? login.name : "";
    }

    public int getLoginId(){
        return login != null ? login.id : 0;
    }
}
