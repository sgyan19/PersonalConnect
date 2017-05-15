package com.sun.connect;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by guoyao on 2017/5/15.
 * 只是为了入库
 */
@Entity(indexes = {
        @Index(value = "id ASC", unique = true)})
public class ResponseNote {
    @Id(autoincrement = true)
    private Long id;
    private String requestId;
    private String json;

    @Generated(hash = 552432290)
    public ResponseNote(Long id, String requestId, String json) {
        this.id = id;
        this.requestId = requestId;
        this.json = json;
    }

    public ResponseNote(String requestId, String json) {
        this.requestId = requestId;
        this.json = json;
        this.id = null;
    }

    @Generated(hash = 515974279)
    public ResponseNote() {
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
