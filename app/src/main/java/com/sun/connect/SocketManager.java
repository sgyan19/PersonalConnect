package com.sun.connect;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by guoyao on 2016/12/13.
 */
public class SocketManager {
    private static SocketManager instance;
    private static SocketManager getInstance(){
        if(instance == null){
            instance = new SocketManager();
        }
        return instance;
    }
    protected SocketManager(){
    }


    private LinkedList<ClientSocket> SocketList = new LinkedList<>();
    private ThreadPoolExecutor mThreadPoolExecutor;


    public ClientSocket getConnectSocket(){
        ClientSocket result = null;
        Iterator<ClientSocket> iterator = SocketList.iterator();
        while(iterator.hasNext()){
            ClientSocket socket = iterator.next();
            if(socket == null){
                iterator.remove();
                continue;
            }
            if(socket.isConnected()){
                result = socket;
                break;
            }else{
                try{
                    boolean suc = socket.connect();
                    if(suc){
                        result = socket;
                        break;
                    }
                }catch (Exception e){}
                iterator.remove();
            }
        }
        if(result == null){
            result = new ClientSocket();
            if(result.connect()) {
                SocketList.add(result);
            }
        }
        return result;
    }
}
