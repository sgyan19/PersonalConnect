package com.sun.connect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOError;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoyao on 2016/12/13.
 */
public class SocketTask implements Runnable {
    public static final int MSG_CONNECT = 0;
    public static final int MSG_REQUEST = 1;
    public static final int MSG_RECEIVE = 2;
    public static final int MSG_STOP_RECIVE = 3;
    public static final int MSG_DISCONNECT = 9;

    public static final int MODEL_NORMAL = 1;
    public static final int MODEL_DUPLEX = 2;

    private ClientSocket mCoreSocket = new ClientSocket();
    private Thread mCoreThread;
    private SocketHandler mHandler;
    private Runnable mReadyRunnable;
    private static SocketTask instance;
    private static int MessageId = 0;
    private int mModel = MODEL_NORMAL;
    private ExecutorService mReceiveExecutor;
    private boolean mReceive = false;
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
        mHandler = new SocketHandler();
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

    private static class SocketHandler extends Handler{
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
                case MSG_RECEIVE:
                    connected = getInstance().makeSureConnected(msg.arg1,(MessageData)msg.obj);
                    if(connected){
                        getInstance().startReceive((MessageData) msg.obj);
                    }
                    break;
                case MSG_STOP_RECIVE:
                    connected = getInstance().makeSureConnected(msg.arg1,(MessageData)msg.obj);
                    if(connected){
                        getInstance().stopReceive();
                    }
                    break;
                case MSG_DISCONNECT:
                    getInstance().mCoreSocket.Close();
                    break;
            }
        }
    }
    private SocketCallback mDupLexCallback;
    private void startReceive(MessageData data){
        mModel = MODEL_DUPLEX;
        if(mReceiveExecutor == null) {
            mReceiveExecutor = Executors.newSingleThreadExecutor();
        }
        mDupLexCallback = data.callback;
        mReceive = true;
        mReceiveExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while(mReceive) {
                    if(!mCoreSocket.isConnected()){
                        break;
                    }
                    try {
                        ResponseData data = mCoreSocket.receive();
                        if(mDupLexCallback != null){
                            mDupLexCallback.onComplete(-1, data);
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
    private void stopReceive(){
        mReceive = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mReceiveExecutor != null && !mReceiveExecutor.isShutdown()){
                    mReceiveExecutor.shutdown();
                }
            }
        },2000);
    }
    public boolean makeSureConnected(int id, MessageData data){
        boolean connected = mCoreSocket.isConnected() || mCoreSocket.connect();
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
        if(mModel == MODEL_NORMAL) {
            ResponseData data = mCoreSocket.request(messageData.requestData);
            if (messageData.callback != null) {
                if (data == null) {
                    messageData.callback.onError(id, mCoreSocket.getLastException());
                } else {
                    messageData.callback.onComplete(id, data);
                }
            }
        }else {
            mCoreSocket.requestWithoutBack(messageData.requestData);
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
