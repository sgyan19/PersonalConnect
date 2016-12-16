package com.sun.conversation;

import android.content.Context;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoyao on 2016/12/16.
 */
public class CvsHistoryManager {
    /** A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher. */
    public static final boolean ENCRYPTED = false;

    public static String DbName;

    public DaoMaster.DevOpenHelper devOpenHelper;
    private DaoSession daoSession;

    private CvsNoteDao mCvsDao; // CvsNoteDao 由GreenDao自动生成，一般位于build/generated目录
    private Query<CvsNote> mCvsLast10TimeDescQuery;

    private List<CvsNote> mCvsCache;

    public void init(Context context){
        DbName = context.getPackageName();
        devOpenHelper = new DaoMaster.DevOpenHelper(context, ENCRYPTED ? DbName + "-db-encrypted" : DbName +"-db");
        Database db = ENCRYPTED ? devOpenHelper.getEncryptedWritableDb("super-secret") : devOpenHelper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        mCvsDao = daoSession.getCvsNoteDao();

        mCvsLast10TimeDescQuery = mCvsDao.queryBuilder().orderDesc(CvsNoteDao.Properties.TimeStamp).limit(10).build();
        mCvsCache = mCvsLast10TimeDescQuery.list();
        if(mCvsCache == null){
            mCvsCache = new ArrayList<>();
        }
    }

    public List<CvsNote> getCache(){
        return mCvsCache;
    }

    public CvsNote getCache(int index){
        return mCvsCache.get(index);
    }

    public int getCacheCount(){
        return mCvsCache.size();
    }

    public void insertCache(CvsNote note){
        mCvsCache.add(note);
    }
}
