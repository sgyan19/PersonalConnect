package com.sun.connect;

import android.util.Log;

import com.sun.settings.Config;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * Created by guoyao on 2016/12/13.
 */
public class ClientSocket {
    public static final String TAG = "ClientSocket";
    public static final Host[] HostList = new Host[]{
            new Host(0,"192.168.137.1"),
            new Host(0,"maths326009812.oicp.net"),
    };

    public static Host Host;
    public static final int Port = 19193;
    public static final byte HeartBeatASK = 0x19;
    public static final byte HeartBeatANS = (byte)0x91;
    public final Object lock = new Object();

    public static class Host{
        public int tryTimes;
        public String address;

        public Host(int times, String addr){
            this.tryTimes = times;
            this.address = addr;
        }
    }

    private byte[] buffer = new byte[1024 * 10];
    private byte[] mHeartBeatData = new byte[]{HeartBeatASK};
    private Socket mSocket;
    private Throwable mLastException;

    private boolean mRemoteClosed = true;

    public Throwable getLastException() {
        return mLastException;
    }

    public boolean connect()
    {
        boolean connected = false;
        Log.d(TAG, "尝试连接，开始进入锁区");
        synchronized (lock) {
            Log.d(TAG, "已进入锁区");
            if(mSocket != null && mRemoteClosed && !mSocket.isClosed()){
                try {
                    Log.d(TAG, mSocket.getInetAddress().getHostAddress() + " 远程已断开");
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(mSocket == null|| mSocket.isClosed()|| mRemoteClosed) {
                try {
                    Host = choseHost();
                    Log.d(TAG, Host.address + " 重连");
                    mSocket = new Socket(Host.address, Port);
                    mSocket.setKeepAlive(true);
                    Log.d(TAG, Host.address + " 连接成功");
                    Host.tryTimes = 0;
                    connected = true;
                    mRemoteClosed = false;
                } catch (Exception e) {
                    Log.d(TAG, Host.address + " 连接失败");
                    mRemoteClosed = true;
                    mLastException = e;
                    Host.tryTimes++;
                    connected = false;
                }
            }else{
                Log.d(TAG, mSocket.getInetAddress().getHostAddress() + " 已连接");
            }
        }
        return connected;
    }

    public String request(String request)
    {
        String responseData = null;
        synchronized (lock) {
            try {
                OutputStreamWriter writer = new OutputStreamWriter(
                        mSocket.getOutputStream(), "utf-8");
                writer.write(request);
                writer.flush();
                Log.d(TAG, "request suc");
                InputStream stream = mSocket.getInputStream();
                int len = stream.read(buffer);
                if (len > 0) {
                    responseData = new String(buffer, 0, len, "utf-8");
                    Log.d(TAG, "request back " + len);
                }
            } catch (IOException e) {
                Log.d(TAG, "request exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }
        }
        return responseData;
    }

    public void requestWithoutBack(String request){
        synchronized (lock) {
            try {
                OutputStreamWriter writer = new OutputStreamWriter(
                        mSocket.getOutputStream(), "utf-8");
                writer.write(request);
                writer.flush();
                Log.d(TAG, "requestWithoutBack suc");
            } catch (IOException e) {
                Log.d(TAG, "requestWithoutBack exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }
        }
    }

    public String receive() throws IOException{
        String responseData = null;
        InputStream stream = mSocket.getInputStream();
        int len  = stream.read(buffer);

        if(len == 1 && buffer[0] == HeartBeatANS){
            Log.d(TAG, "HeartBeatANS");
        }else if(len > 0) {
            responseData = new String(buffer, 0, len, "utf-8");
        }else{
            mRemoteClosed = true;
        }
        return responseData;
    }

    public boolean heartbeat(){
        synchronized (lock) {
            try {
                OutputStream outputStream = mSocket.getOutputStream();
                outputStream.write(mHeartBeatData);
                outputStream.flush();
                Log.d(TAG, "heartbeatASK");
                mRemoteClosed = false;
            } catch (IOException e) {
                Log.d(TAG, "heartbeat exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }
        }
        return !mRemoteClosed;
    }

    public boolean isConnecting()
    {
        if (mSocket != null)
        {
            return !mSocket.isClosed() && !mRemoteClosed;
        }
        return false;
    }

    public void Close()
    {
        synchronized (lock) {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (Exception e) {
                    mLastException = e;
                    e.printStackTrace();
                }
            }
        }
    }

    private Host choseHost(){
//        for(int i = 0 ; i < HostList.length; i ++){
//            if(HostList[i].tryTimes < 3){
//                return HostList[i];
//            }
//        }
        return HostList[0].tryTimes < HostList[1].tryTimes ? HostList[0] : HostList[1];
    }
}
