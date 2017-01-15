package com.sun.connect;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
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
    public static final byte HEADER_BIN = (byte)0x2B;
    public static final byte HEADER_JSON = (byte)0x7B;

    public final Object lock = new Object();
    private static String mRawDir = "/sdcard/ClientSocket";
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

    public SocketMessage request(String request)
    {
        SocketMessage response = new SocketMessage();
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
                    response.data = new String(buffer, 0, len, "utf-8");
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
        return response;
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

    public SocketMessage receive() throws IOException{
        SocketMessage response = new SocketMessage();
        InputStream stream = mSocket.getInputStream();
        int len  = stream.read(buffer,0,1);
        if(len >= 1){
            if(buffer[0] == HeartBeatANS) {
                Log.d(TAG, "HeartBeatANS");
            }else if(buffer[0] == HEADER_BIN){
                response.type = SocketMessage.SOCKET_TYPE_RAW;
                Log.d(TAG, "HEADER_BIN");
                receiveTextFrame(stream, response);
                receiveRawFrame(stream, response.data, response);
            }else {
                response.type = SocketMessage.SOCKET_TYPE_JSON;
                receiveTextFrame(stream, response);
            }
        }else{
            mRemoteClosed = true;
        }
        return response;
    }

    private void receiveTextFrame(InputStream stream, SocketMessage response) throws IOException{
        int len = stream.read(buffer, 0, 4);
        int size = bytesToInt(buffer, 0);
        if(len > buffer.length ){
            response.exception = new SocketException("TextFrame size overstep the boundary");
            return;
        }
        int offset = 0;
        int old = mSocket.getSoTimeout();
        mSocket.setSoTimeout(1000);
        try {
            while ((len = stream.read(buffer, offset, size)) != 0) {
                offset += len;
                size = size - len;
            }
        }catch (SocketException e){
        }
        response.data = new String(buffer, 0, size, "utf-8");
        mSocket.setSoTimeout(old);
    }

    private void receiveRawFrame(InputStream stream, String name, SocketMessage response) throws IOException{
        stream.read(buffer, 0, 4);
        int size = bytesToInt(buffer, 0);
        int old = mSocket.getSoTimeout();
        int len;
        mSocket.setSoTimeout(1000);
        try {
            File file = new File(mRawDir, name);
            if(file.exists()){
                file.delete();
            }
            OutputStream fileStream = null;
            try {
                fileStream = new FileOutputStream(file);
                while (size > 0) {
                    len = stream.read(buffer,0, size > buffer.length ? buffer.length: size);
                    if(len == 0) break;
                    size = size - len;
                    fileStream.write(buffer, 0, len);
                }
            }finally {
                if(fileStream != null) {
                    try {
                        fileStream.close();
                    }catch (Exception e){}
                }
            }
            response.data = file.getName();
        }catch (SocketException e){
        }
        response.data = name;
        response.type = SocketMessage.SOCKET_TYPE_RAW;
        mSocket.setSoTimeout(old);
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

    public static void setRawFolder(String path){
        mRawDir = path;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src
     *            byte数组
     * @param offset
     *            从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }
}
