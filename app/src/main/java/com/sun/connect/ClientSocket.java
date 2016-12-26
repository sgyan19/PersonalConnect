package com.sun.connect;

import android.util.Log;

import com.sun.settings.Config;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by guoyao on 2016/12/13.
 */
public class ClientSocket {
    public static final String TAG = "ClientSocket";
    public static String Host;
    public static final int Port = 19193;

    private byte[] buffer = new byte[1024 * 10];

    private Socket mSocket;
    private Throwable mLastException;

    private boolean mRemoteClosed = true;

    public Throwable getLastException() {
        return mLastException;
    }

    public boolean connect()
    {
        if(mSocket != null &&(mSocket.isClosed()|| mRemoteClosed)) {
            try {
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try
        {
            Host = Config.Debug ? "192.168.137.1" :"hanclt.eicp.net";
            Log.d(TAG, Host + " 重连");
            mSocket = new Socket(Host, Port);
        }
        catch(Exception e)
        {
            Log.d(TAG, Host + " 连接失败");
            mRemoteClosed = true;
            mLastException = e;
            return false;
        }
        mRemoteClosed = false;
        return true;
    }

    public String request(String request)
    {
        String responseData = null;
        try{
            OutputStreamWriter writer = new OutputStreamWriter(
                    mSocket.getOutputStream(), "utf-8");
            writer.write(request);
            writer.flush();
            InputStream stream = mSocket.getInputStream();
            int len  = stream.read(buffer);
            if(len > 0) {
                responseData = new String(buffer, 0, len, "utf-8");
            }
        }catch (IOException e){
            mLastException = e;
            if(e instanceof SocketException){
                mRemoteClosed = true;
            }
            e.printStackTrace();
        }
        return responseData;
    }

    public void requestWithoutBack(String request){
        try{
            OutputStreamWriter writer = new OutputStreamWriter(
                    mSocket.getOutputStream(), "utf-8");
            writer.write(request);
            writer.flush();
        }catch (IOException e){
            mLastException = e;
            if(e instanceof SocketException){
                mRemoteClosed = true;
            }
            e.printStackTrace();
        }
    }

    public String receive() throws IOException{
        String responseData = null;
        InputStream stream = mSocket.getInputStream();
        int len  = stream.read(buffer);
        if(len > 0) {
            responseData = new String(buffer, 0, len, "utf-8");
        }else{
            mRemoteClosed = true;
        }
        return responseData;
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
        if(mSocket != null)
        {
            try
            {
                mSocket.close();
            }
            catch (Exception e)
            {
                mLastException = e;
                e.printStackTrace();
            }
        }
    }
}
