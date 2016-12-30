package com.sun.connect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoyao on 2016/12/13.
 */
public class SocketTask implements Runnable {
    public static final String TAG = "SocketTask";
    public static final int MSG_CONNECT = 0;
    public static final int MSG_REQUEST = 1;
    public static final int MSG_RECEIVE = 2;
    public static final int MSG_STOP_RECEIVE = 3;
    public static final int MSG_DISCONNECT = 9;

    public static final int REQUEST_KEY_NOBODY = -1;
    public static final int REQUEST_KEY_ANYBODY = 0;

    private ClientSocket mCoreSocket = new ClientSocket();
    private Thread mCoreThread;
    private SocketHandler mHandler;
    private List<Runnable> mThreadReadyListener = new LinkedList<>();
    private final Object ReceiveLock = new Object();
    private ExecutorService mReceiveExecutor;
    private boolean mReceiving = false;

    public void start(){
        start(null);
    }
    public void start(Runnable runnable){
        if(mCoreThread == null) {
            mCoreThread = new Thread(this);
            //mLooper = new Looper();
            mCoreThread.start();
            if(runnable != null){
                mThreadReadyListener.add(runnable);
            }
        }
    }

    public void startWithReceive(SocketCallback callback){
        mDupLexCallback = callback;
        start(new Runnable() {
            @Override
            public void run() {
                startReceive(mDupLexCallback);
            }
        });
    }

    public void Ready(Runnable runnable){
        if(runnable == null){
            return;
        }
        if(mCoreThread != null){
             runnable.run();
        }else{
            mThreadReadyListener.add(runnable);
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new SocketHandler(this);
        for(Runnable run : mThreadReadyListener) {
            if (run != null) {
                run.run();
            }
        }
        mThreadReadyListener.clear();
        Looper.loop();
        mCoreThread = null;
    }

    public static class MessageData{
        private SocketCallback callback;
        private String requestData;

        public MessageData(SocketCallback callback, String data){
            this.callback = callback;
            this.requestData = data;
        }
    }

    private static class SocketHandler extends Handler{
        private WeakReference<SocketTask> parent;

        SocketHandler(SocketTask parent) {
            this.parent = new WeakReference<>(parent);
        }
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            SocketTask st = parent.get();
            if(st == null){
                return;
            }
            boolean connected;
            SocketCallback callback = st.mReceiving ? st.mDupLexCallback :((msg.obj != null && msg.obj instanceof MessageData)? ((MessageData) msg.obj).callback : null);

            Log.d(TAG, "handleMessage:" + (msg.obj == null ? msg.arg1 :((MessageData) msg.obj).requestData));
            switch (what){
                case MSG_CONNECT:
                    st.makeSureConnected(msg.arg1, callback, callback,null);
                    break;
                case MSG_REQUEST:
                    connected = st.makeSureConnected(msg.arg1, null,callback,null);
                    if(connected){
                        st.request(msg.arg1, (MessageData) msg.obj);
                    }
                    break;
                case MSG_RECEIVE:
                    st.startReceive(callback);
                    break;
                case MSG_STOP_RECEIVE:
                    st.stopReceive();
                    break;
                case MSG_DISCONNECT:
                    st.stopReceive();
                    st.mCoreSocket.Close();
                    break;
            }
        }
    }

    private SocketCallback mDupLexCallback;
    private void startReceive(SocketCallback callback){
        if(mReceiveExecutor == null) {
            mReceiveExecutor = Executors.newSingleThreadExecutor();
        }
        mDupLexCallback = callback;
        mReceiving = true;
        mReceiveExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Receive 开始");
                while (mReceiving) {
                    if (!makeSureConnected(REQUEST_KEY_NOBODY, null, null, mDupLexCallback)) {
                        Log.d(TAG, "Receive 未能连接 wait 20 s");
                        synchronized (ReceiveLock) {
                            try {
                                ReceiveLock.wait(20000);
                            } catch (InterruptedException e) {
                            }
                        }
                        continue;
                    }
                    try {
                        Log.d(TAG, "Receive 已连接 开始receive");
                        String response = mCoreSocket.receive();
                        Log.d(TAG, "Receive response:" + response);
                        if (response != null) {
                            if (mDupLexCallback != null) {
                                mDupLexCallback.onComplete(REQUEST_KEY_ANYBODY, response);
                            }
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "Receive 异常");
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Receive stop");
            }
        });
    }
    public void stopReceive(){
        mReceiving = false;
        mDupLexCallback = null;
        synchronized (ReceiveLock){
            ReceiveLock.notifyAll();
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mReceiveExecutor != null && !mReceiveExecutor.isShutdown()) {
                    mReceiveExecutor.shutdown();
                }
            }
        });
    }

    public boolean makeSureConnected(int id, SocketCallback sucBack, SocketCallback errBack,SocketCallback onConnectCallback){
        boolean connected ;
        if(mCoreSocket.isConnecting()){
            connected = true;
        }else{
            connected = mCoreSocket.connect();
            if(connected) {
                if (onConnectCallback != null) {
                    onConnectCallback.onConnected(id);
                }
                if (mReceiving) {
                    synchronized (ReceiveLock) {
                        ReceiveLock.notifyAll();
                    }
                }
            }
        }
        if(connected){
            if(sucBack != null) {
                sucBack.onComplete(id, null);
            }
        }else{
            if(errBack != null) {
                errBack.onError(id, mCoreSocket.getLastException());
            }
        }
        return connected;
    }

    private void request(int id, MessageData messageData){
        Log.d(TAG, "request data" + messageData.requestData);
        if(mReceiving) {
            mCoreSocket.requestWithoutBack(messageData.requestData);
        }else {
            String response = mCoreSocket.request(messageData.requestData);
            if (messageData.callback != null) {
                if (response == null) {
                    messageData.callback.onError(id, mCoreSocket.getLastException());
                } else {
                    messageData.callback.onComplete(id, response);
                }
            }
        }
    }

    public void sendMessage(int what, int key, String data, SocketCallback callback){
        Message msg = new Message();
        msg.arg1 = key;
        msg.what = what;
        msg.obj = new MessageData(callback, data);
        mHandler.sendMessage(msg);
    }

    public void quitThreadLooper(){
        sendMessage(MSG_DISCONNECT, REQUEST_KEY_NOBODY, null, null);
    }
}
