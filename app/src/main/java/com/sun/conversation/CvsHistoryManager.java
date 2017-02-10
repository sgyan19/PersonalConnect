package com.sun.conversation;

import android.content.Context;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
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
    private List<CvsNote> mWaitForSave;

    private CvsNote mLastSendNote;

    public void init(Context context){
        DbName = context.getPackageName();
        devOpenHelper = new DaoMaster.DevOpenHelper(context, ENCRYPTED ? DbName + "-db-encrypted" : DbName +"-db");
        Database db = ENCRYPTED ? devOpenHelper.getEncryptedWritableDb("super-secret") : devOpenHelper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        mCvsDao = daoSession.getCvsNoteDao();

        mCvsLast10TimeDescQuery = mCvsDao.queryBuilder().orderDesc(CvsNoteDao.Properties.TimeStamp).limit(15).build();
        mCvsCache = mCvsLast10TimeDescQuery.list();
        Collections.reverse(mCvsCache);
        if(mCvsCache == null){
            mCvsCache = new ArrayList<>();
        }
        mWaitForSave = new LinkedList<>();
    }

    public List<CvsNote> getCache(){
        return mCvsCache;
    }

    public CvsNote getLastCache(){
        return mCvsCache.get(mCvsCache.size() - 1);
    }

    public CvsNote getCache(int index){
        return mCvsCache.get(index);
    }

    public int getCacheCount(){
        return mCvsCache.size();
    }

    public void insertCache(CvsNote note){
        mCvsCache.add(note);
//        mWaitForSave.add(note);
        mCvsDao.insert(note);
    }

    public boolean updateCache(long id){
        Iterator<CvsNote> iterator = mCvsCache.iterator();
        boolean update = false;
        while(iterator.hasNext()){
            CvsNote note = iterator.next();
            if(note.getId() == id){
//                mCvsDao.insert(note);
                mCvsDao.update(note);
                update = true;
                break;
            }
        }
        return update;
    }

    public void saveCache(){
        Iterator<CvsNote> iterator = mWaitForSave.iterator();
        while(iterator.hasNext()){
            CvsNote note = iterator.next();
            if(note.getSendStatus() != CvsNote.STATUS_INIT){
                mCvsDao.insert(note);
                iterator.remove();
            }
        }
    }

    public int removeTooMoreCache(){
        int removeCount = mCvsCache.size() - 10;
        if(removeCount <=  0){
            return 0;
        }
        int index = 0;
        LinkedList<CvsNote> tmp = new LinkedList<>();
        tmp.addAll(mCvsCache);
        Iterator<CvsNote> iterator = tmp.iterator();
        while(iterator.hasNext() && index < removeCount){
            CvsNote note = iterator.next();
            if(note.getSendStatus() == CvsNote.STATUS_SUC){
                iterator.remove();
                index++;
            }
        }
        mCvsCache.clear();
        mCvsCache.addAll(tmp);
        tmp.clear();
        return index;
    }

    public void close(){
        if(devOpenHelper != null) {
            devOpenHelper.close();
        }
    }

    public void keepLastSendNote(CvsNote note){
        mLastSendNote = note;
    }

    public CvsNote getLastSendNote(){
        return mLastSendNote;
    }
}
