package com.sun.connect;
import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Created by guoyao on 2017/5/15.
 */
public class ResponseHistoryManager {
    private ResponseNoteDao mDao; // CvsNoteDao 由GreenDao自动生成，一般位于build/generated目录

    public void init(DaoSession daoSession){
        mDao = daoSession.getResponseNoteDao();
    }

    public boolean insert(ResponseNote note){
        Query<ResponseNote> query =  mDao.queryBuilder()
                .where(ResponseNoteDao.Properties.RequestId.eq(note.getRequestId())).build();
        List<ResponseNote> list = query.list();
        if(list.isEmpty()){
            mDao.insert(note);
//            if()

            return true;
        }
        return false;
    }
}
