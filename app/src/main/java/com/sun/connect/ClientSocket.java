package com.sun.connect;

import android.util.Log;

import com.sun.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by guoyao on 2016/12/13.
 */
public class ClientSocket {
    public static final String TAG = "ClientSocket";
    public static final Host[] HostList = new Host[]{
            new Host(0,"hanclt.eicp.net"),
            new Host(0,"maths326009812.oicp.net"),
    };

    public static Host Host;
    public static final int Port = 19193;
    public static final byte[] HeartBeatASK = new byte[]{(byte)0x19, 0x19 +1, 0x19 +2 ,0x19 + 3};
    public static final byte[] HeartBeatANS = new byte[]{(byte)0x91, (byte)0x91 +1, (byte)0x91 +2 ,(byte)0x91 + 3};
    public static final byte[] HEADER_RAW = new byte[]{(byte)0x2B, 0x2B +1, 0x2B +2 ,0x2B + 3};
    public static final byte[] HEADER_CKRAW = new byte[]{(byte)0x3B, 0x3B +1, 0x3B +2 ,0x3B + 3};
    public static final byte HEADER_CK_SUC_RAW = (byte)0x3C;
    public static final byte HEADER_CK_FAIL_RAW = (byte)0x3D;
    public static final byte[] HEADER_JSON = new byte[]{(byte)0x7B, 0x7B +1, 0x7B +2 ,0x7B + 3};

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

    private byte[] sendBuffer = new byte[1024 * 10];
    private byte[] recBuffer = new byte[1024 * 10];
    private byte[] mHeartBeatData = HeartBeatASK;
    private Socket mSocket;
    private Throwable mLastException;

    private boolean mRemoteClosed = true;

    public Throwable getLastException() {
        return mLastException;
    }

    private long mLastConnectClock = 0 ;

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
                    mLastException = e;
                }
            }
            if(mSocket == null|| mSocket.isClosed()|| mRemoteClosed) {
                long lastTryGap = System.nanoTime() - mLastConnectClock;
                Log.d(TAG, "lastTryGap :" + lastTryGap);
                if(lastTryGap < 10000000){
                    Log.d(TAG, Host.address + " 接近上次重连时间，不再重试");
                    connected = false;
                }else {
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
                        mLastConnectClock = System.nanoTime();
                    }
                }
            }else{
                Log.d(TAG, mSocket.getInetAddress().getHostAddress() + " 已连接");
            }
        }
        return connected;
    }

    public SocketMessage requestJson(String request)
    {
        SocketMessage response = null;
        synchronized (lock) {
            try {
                OutputStream outputStream = mSocket.getOutputStream();
                outputStream.write(HEADER_JSON);
                sendTextFrame(outputStream, request);
                Log.d(TAG, "requestJson suc");
                response = receive(20000);
            } catch (IOException e) {
                Log.d(TAG, "requestJson exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }
        }
        return response;
    }

    public SocketMessage requestRaw(String name){
        SocketMessage response = null;
        synchronized (lock) {
            try {
                OutputStream outputStream = mSocket.getOutputStream();
                outputStream.write(HEADER_RAW);
                sendTextFrame(outputStream, name);
                Log.d(TAG, "requestRaw name suc");
                sendRawFrame(outputStream, name);
                Log.d(TAG, "requestRaw file suc");
                response = receive(20000);
            } catch (IOException e) {
                Log.d(TAG, "requestRaw exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }
        }
        return response;
    }

    public SocketMessage requestCheckRaw(String name){
        SocketMessage response = null;
        synchronized (lock) {
            try {
                OutputStream outputStream = mSocket.getOutputStream();
                outputStream.write(HEADER_CKRAW);
                sendTextFrame(outputStream, name);
                Log.d(TAG, "requestRaw name suc");

                File file = new File(mRawDir, name);
                String md5 = Utils.md5(file);
                sendTextFrame(outputStream, md5);
                Log.d(TAG, "requestRaw md5 suc");

                InputStream inputStream = mSocket.getInputStream();
                mSocket.setSoTimeout(20000);
                int len  = inputStream.read(recBuffer,0,1);
                if(len > 0 ){
                    if(recBuffer[0] == HEADER_CK_SUC_RAW) {
                        Log.d(TAG, "receive md5 check suc");
                        response = SocketMessage.EmptyMessage;
                    }else if(recBuffer[0] == HEADER_CK_FAIL_RAW){
                        Log.d(TAG, "receive md5 check fail");
                        response = requestRaw(name);
                    }else {
                        Log.d(TAG, "receive md5 check unknown code:" + recBuffer[0]);
                    }
                }else {
                    Log.d(TAG, "receive md5 check fail len =" + len);
                }
            } catch (IOException e) {
                Log.d(TAG, "requestRawWithoutBack exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }finally {
                try {
                    mSocket.setSoTimeout(0);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    public void requestJsonWithoutBack(String request){
        synchronized (lock) {
            try {
                OutputStream outputStream = mSocket.getOutputStream();
                outputStream.write(HEADER_JSON);
                sendTextFrame(outputStream, request);
                Log.d(TAG, "requestJsonWithoutBack suc");
            } catch (IOException e) {
                Log.d(TAG, "requestJsonWithoutBack exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }
        }
    }

    public void requestRawWithoutBack(String name){
        synchronized (lock) {
            try {
                OutputStream outputStream = mSocket.getOutputStream();
                outputStream.write(HEADER_RAW);
                sendTextFrame(outputStream, name);
                Log.d(TAG, "requestRawWithoutBack name suc");
                sendRawFrame(outputStream, name);
                Log.d(TAG, "requestRawWithoutBack file suc");
            } catch (IOException e) {
                Log.d(TAG, "requestRawWithoutBack exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }
        }
    }

    public SocketMessage receive(int timeOut) throws IOException{
        Log.d(TAG, "start receive()");
        int oldTimeOut = mSocket.getSoTimeout();
        mSocket.setSoTimeout(timeOut);
        SocketMessage response = new SocketMessage();
        InputStream stream = mSocket.getInputStream();
        int len  = stream.read(recBuffer,0,4);
        if(len >= 1){
            if(recBuffer[0] == HeartBeatANS[0] && recBuffer[1] == HeartBeatANS[1]&& recBuffer[2] == HeartBeatANS[2]&& recBuffer[3] == HeartBeatANS[3]) {
                Log.d(TAG, "HeartBeatANS");
            }else if(recBuffer[0] == HEADER_RAW[0] && recBuffer[1] == HEADER_RAW[1]&& recBuffer[2] == HEADER_RAW[2]&& recBuffer[3] == HEADER_RAW[3]){
                response.type = SocketMessage.SOCKET_TYPE_RAW;
                Log.d(TAG, "HEADER_RAW");
                response.data = receiveTextFrame(stream);
                Log.d(TAG, "receiveTextFrame suc:" + response.data);
                receiveRawFrame(stream, response.data);
            }else if(recBuffer[0] == HEADER_JSON[0] && recBuffer[1] == HEADER_JSON[1]&& recBuffer[2] == HEADER_JSON[2]&& recBuffer[3] == HEADER_JSON[3]){
                Log.d(TAG, "HEADER_JSON");
                response.type = SocketMessage.SOCKET_TYPE_JSON;
                response.data = receiveTextFrame(stream);
                Log.d(TAG, "receiveTextFrame suc:" + response.data);
            }else{
                Log.d(TAG, String.format("unknown code:%d,%d,%d,%d" , recBuffer[0],recBuffer[1],recBuffer[2],recBuffer[3]));
                receiveTrash(stream);
                response = null;
            }
        }else{
            Log.d(TAG, "len < 1" );
            mRemoteClosed = true;
        }
        mSocket.setSoTimeout(oldTimeOut);
        return response;
    }

    private void sendTextFrame(OutputStream stream, String text) throws IOException{
        try {
            byte[] data = text.getBytes("utf-8");
            intToBytes(data.length, sendBuffer, 0);
            stream.write(sendBuffer, 0, 4);
            stream.write(data,0 , data.length);
            stream.flush();
        }catch (UnsupportedEncodingException e){
            mLastException = e;
            e.printStackTrace();
        }
    }

    private void sendRawFrame(OutputStream stream, String fileName) throws IOException{
        File file = new File(mRawDir, fileName);
        int size = (int)file.length();
        intToBytes(size, sendBuffer, 0);
        stream.write(sendBuffer, 0, 4);

        InputStream fileStream = null;
        try {
            int len;
            long total = 0;
            fileStream = new FileInputStream(file);
            while((len = fileStream.read(sendBuffer, 0, size > sendBuffer.length ? sendBuffer.length: size)) > 0){
                total += len;
                stream.write(sendBuffer, 0, len);
                Log.d(TAG, String.format("send RawFrame len:%d total:%d size:%d", len,total, size));
            }
        }finally {
            if(fileStream != null){
                try {
                    fileStream.close();
                }catch (IOException e){
                    mLastException = e;
                    e.printStackTrace();
                }
            }
        }
        stream.flush();
    }

    private void sendRawFrame(OutputStream stream, byte[] data) throws IOException{
        int size = data.length;
        intToBytes(size, sendBuffer, 0);
        stream.write(sendBuffer, 0, 4);
        stream.write(data);
        stream.flush();
    }

    private String receiveTextFrame(InputStream stream) throws IOException{
        mSocket.setSoTimeout(10000);
        int old = mSocket.getSoTimeout();
        stream.read(recBuffer, 0, 4);
        int size = bytesToInt(recBuffer, 0);
        Log.d(TAG, String.format("read receiveTextFrame size:%d", size));
        if(size > recBuffer.length || size <= 0){
            throw new SocketException("TextFrame size overstep the boundary || size < 0");
        }
        int offset = 0;
        mSocket.setSoTimeout(2000);
        int len;
        try {
            while ((len = stream.read(recBuffer, offset, size)) != 0) {
                offset += len;
                size = size - len;
                Log.d(TAG, String.format("read lastSize:%d, len:%d offset:%d", size, len, offset));
            }
        }catch (SocketTimeoutException e){
        }
        mSocket.setSoTimeout(old);
        return new String(recBuffer, 0, offset, "utf-8");
    }

    private void receiveRawFrame(InputStream stream, String name) throws IOException{
        Log.d(TAG, "receiveRawFrame into");
        int old = mSocket.getSoTimeout();
        mSocket.setSoTimeout(10000);
        stream.read(recBuffer, 0, 4);
        int size = bytesToInt(recBuffer, 0);
        Log.d(TAG, String.format("read receiveRawFrame size:%d", size));
        int len;
        mSocket.setSoTimeout(2000);
        try {
            File file = new File(mRawDir, name);
            if(file.exists()){
                file.delete();
            }
            OutputStream fileStream = null;
            try {
                fileStream = new FileOutputStream(file);
                while (size > 0) {
                    len = stream.read(recBuffer,0, size > recBuffer.length ? recBuffer.length: size);
                    if(len <= 0) break;
                    size = size - len;
                    fileStream.write(recBuffer, 0, len);
                    Log.d(TAG, String.format("read lastSize:%d, len:%d", size, len));
                }
            }finally {
                if(fileStream != null) {
                    try {
                        fileStream.close();
                    }catch (Exception e){}
                }
            }
        }catch (SocketException e){
        }
        mSocket.setSoTimeout(old);
    }

    private void receiveTrash(InputStream stream) throws IOException{
        receiveTrash(stream,5000);
    }

    private void receiveTrash(InputStream stream, int timeout) throws IOException{
        int len;
        int old = mSocket.getSoTimeout();
        mSocket.setSoTimeout(timeout);
        try {
            while ((len = stream.read(recBuffer)) > 0) {
                Log.d(TAG, String.format("receiveTrash len:%d", len));
            }
        }catch (IOException e){
//            e.printStackTrace();
            Log.d(TAG, String.format("known exception: %s", e.toString()));
            mLastException = e;
        }
        mSocket.setSoTimeout(old);
    }

    public boolean heartbeatAsync(){
        Log.d(TAG, "heartbeatAsync");
        synchronized (lock) {
            try {
                OutputStream outputStream = mSocket.getOutputStream();
                outputStream.write(mHeartBeatData);
                outputStream.flush();
                Log.d(TAG, "heartbeatASK");
                mRemoteClosed = false;
            } catch (IOException e) {
                Log.d(TAG, "heartbeatAsync exception:" + e.toString());
                mLastException = e;
                if (e instanceof SocketException) {
                    mRemoteClosed = true;
                }
                e.printStackTrace();
            }
        }
        return !mRemoteClosed;
    }

    public boolean heartbeat(){
        Log.d(TAG, "heartbeat test");
        synchronized (lock) {
            try {
                mSocket.setSoTimeout(0);
                OutputStream outputStream = mSocket.getOutputStream();
                InputStream stream = mSocket.getInputStream();
                receiveTrash(stream, 500);

                outputStream.write(mHeartBeatData);
                outputStream.flush();
                int len  = stream.read(recBuffer,0,4);
                if(len >= 4){
                    if(recBuffer[0] != HeartBeatANS[0] &&recBuffer[1] != HeartBeatANS[1] &&recBuffer[2] != HeartBeatANS[2] &&recBuffer[3] != HeartBeatANS[3]){
                        Log.d(TAG, "not HeartBeatANS" + recBuffer[0]);
                        receiveTrash(stream);
                    }
                }
                Log.d(TAG, "heartbeat over");
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
        int minIndex = 0;
        for(int i = 1 ; i < HostList.length; i ++){
            if(HostList[i].tryTimes < HostList[minIndex].tryTimes){
                minIndex = i;
            }
        }
        return HostList[minIndex];
//        return HostList[0].tryTimes <= HostList[1].tryTimes ? HostList[0] : HostList[1];
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

    public static int intToBytes(int src, byte[] dest, int offset){
        for(int i = 3,j = 0;i >= 0 ;i--,j += 8){
            dest[ offset + i ] = (byte)((src >> (24 - j)) & 0xFF);
        }
        return 4;
    }
}
