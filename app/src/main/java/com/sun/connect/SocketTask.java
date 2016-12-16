package com.sun.connect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashMap;

/**
 * Created by guoyao on 2016/12/13.
 */
public class SocketTask implements Runnable {
    public static final int MSG_CONNECT = 0;
    public static final int MSG_REQUEST = 1;
    public static final int MSG_DISCONNECT = 2;

    private ClientSocket mCoreSocket = new ClientSocket();
    private Thread mCoreThread;
    private EncoderHandler mHandler;
    private Runnable mReadyRunnable;
    private static SocketTask instance;
    private static int MessageId = 0;
    public static SocketTask getInstance(){
        if(instance == null){
            instance = new SocketTask();
        }
        return instance;
    }
    protected SocketTask(){
    }
    public void start(){
        start(null);
    }
    public void start(Runnable runnable){
        mCoreThread = new Thread(this);
        //mLooper = new Looper();
        mCoreThread.start();
        mReadyRunnable = runnable;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new EncoderHandler();
        if(mReadyRunnable != null){
            mReadyRunnable.run();
        }
        Looper.loop();
    }

    public static class MessageData{
        private SocketCallback callback;
        private RequestData requestData;

        public MessageData(SocketCallback callback, RequestData data){
            this.callback = callback;
            this.requestData = data;
        }
    }

    private static class EncoderHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            boolean connected;
            switch (what){
                case MSG_CONNECT:
                    getInstance().makeSureConnected(msg.arg1,  (MessageData)msg.obj);
                    break;
                case MSG_REQUEST:
                    connected = getInstance().makeSureConnected(msg.arg1,(MessageData)msg.obj);
                    if(connected){
                        getInstance().request(msg.arg1, (MessageData)msg.obj);
                    }
                    break;
                case MSG_DISCONNECT:
                    getInstance().mCoreSocket.Close();
                    break;
            }
        }
    }

    public boolean makeSureConnected(int id, MessageData data){
        boolean connected;
        if(!mCoreSocket.isConnected()){
            connected = mCoreSocket.connect();
        }else{
            connected = true;
        }
        if(data.callback != null){
            if(connected ){
                data.callback.onConnect(id);
            }else{
                data.callback.onError(id, mCoreSocket.getLastException());
            }
        }
        return connected;
    }

    public void request(int id, MessageData messageData){
        ResponseData data = mCoreSocket.request(messageData.requestData);
        if(messageData.callback != null) {
            if (data == null) {
                messageData.callback.onError(id, mCoreSocket.getLastException());
            }else{
                messageData.callback.onComplete(id, data);
            }
        }
    }

    public int sendMessage(int what, RequestData data, SocketCallback callback){
        MessageId++;
        Message msg = new Message();
        msg.arg1 = MessageId;
        msg.what = what;
        msg.obj = new MessageData(callback, data);
        mHandler.sendMessage(msg);
        return MessageId;
    }
}
