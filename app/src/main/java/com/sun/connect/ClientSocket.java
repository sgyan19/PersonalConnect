package com.sun.connect;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by guoyao on 2016/12/13.
 */
public class ClientSocket {
    public static final String Host = "hanclt.eicp.net";
    public static final int Port = 19193;

    private byte[] buffer = new byte[1024];

    private Socket mSocket;
    private Throwable mLastException;
    private Gson mGson = new Gson();

    public Throwable getLastException() {
        return mLastException;
    }

    public boolean connect()
    {
        try
        {
            mSocket = new Socket(Host, Port);
        }
        catch(Exception e)
        {
            mLastException = e;
            return false;
        }
        return true;
    }

    public ResponseData request(RequestData data)
    {
        String requestJson = mGson.toJson(data);
        ResponseData responseData = null;
        try{
            OutputStreamWriter writer = new OutputStreamWriter(
                    mSocket.getOutputStream(), "utf-8");
            writer.write(requestJson);
            writer.flush();
            InputStream stream = mSocket.getInputStream();
            int len  = stream.read(buffer);
            String back = new String(buffer,0,len,"utf-8");
            responseData = mGson.fromJson(back, ResponseData.class);
        }catch (IOException e){
            mLastException = e;
            e.printStackTrace();
        }
        return responseData;
    }

    public boolean isConnected()
    {
        if (mSocket != null)
        {
            return mSocket.isConnected();
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
