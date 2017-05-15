package com.sun.personalconnect;

import android.content.Context;

import com.sun.connect.DaoMaster;
import com.sun.connect.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by guoyao on 2017/5/15.
 */
public class DaoSessionManager {
    /** A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher. */
    protected static final boolean ENCRYPTED = false;

    protected String DbName;

    protected DaoSession mDaoSession;

    public DaoMaster.DevOpenHelper devOpenHelper;

    public DaoSession getDaoSession(Context context){
        if(mDaoSession == null) {
            DbName = context.getPackageName();
            devOpenHelper = new DaoMaster.DevOpenHelper(context, ENCRYPTED ? DbName + "-db-encrypted" : DbName + "-db");
            Database db = ENCRYPTED ? devOpenHelper.getEncryptedWritableDb("super-secret") : devOpenHelper.getWritableDb();
            mDaoSession = new DaoMaster(db).newSession();
        }
        return mDaoSession;
    }

    public void release(){
        if(devOpenHelper != null){
            devOpenHelper.close();
        }
//        if(mDaoSession != null){
//            mDaoSession.clear();
//        }
        mDaoSession = null;
    }
}
