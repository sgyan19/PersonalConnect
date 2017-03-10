package com.sun.personalconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by guoyao on 2017/3/7.
 */
public abstract class BaseReceiver<L> extends BroadcastReceiver{

    private static HashMap<Class<? extends BaseReceiver>, StaticHandler<BaseReceiver>> staticHandlerHashMap = new HashMap<>();

    private static class StaticHandler<T>{
        private WeakReference<T> weakReference;
        private T hardReference;
        private LinkedHashMap<Context,Object> listeners = new LinkedHashMap<>();
    }

    protected static void register(Context context, Class<? extends BaseReceiver> clazz, Object listener){
        StaticHandler<BaseReceiver> staticHandler = staticHandlerHashMap.get(clazz);
        if(staticHandler == null){
            staticHandler = new StaticHandler<>();
            staticHandlerHashMap.put(clazz, staticHandler);
        }
        BaseReceiver obj = null;
        if(staticHandler.hardReference == null) {
            if (staticHandler.weakReference == null || staticHandler.weakReference.get() == null) {
                try {
                    obj = clazz.newInstance();
                    staticHandler.weakReference = new WeakReference<>(obj);
                } catch (InstantiationException|IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                obj = staticHandler.weakReference.get();
            }
            staticHandler.hardReference = obj;
        }else{
            obj = staticHandler.hardReference;
        }
        staticHandler.listeners.put(context, listener);
        if(obj == null) return;
        if(!obj.isRegister){
            IntentFilter intentFilter = obj.getIntentFilter();
            context.registerReceiver(obj, intentFilter);
            obj.isRegister = true;
        }
    }

    protected static void unregister(Context context, Class<? extends BaseReceiver> clazz){
        BaseReceiver obj;
        StaticHandler<BaseReceiver> staticHandler = staticHandlerHashMap.get(clazz);
        if(staticHandler == null){
            return;
        }
        if(staticHandler.hardReference == null) {
            if (staticHandler.weakReference == null || (obj = staticHandler.weakReference.get()) == null) {
                return;
            }
        }else{
            obj = staticHandler.hardReference;
        }
        try {
            context.unregisterReceiver(obj);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        staticHandler.listeners.remove(context);
        Set<Context> sets = staticHandler.listeners.keySet();
        if(!sets.isEmpty()){
            Context ctt = sets.iterator().next();
            IntentFilter intentFilter = obj.getIntentFilter();
            ctt.registerReceiver(obj, intentFilter);
        }else{
            obj.isRegister = false;
            staticHandler.hardReference = null;
        }
    }
    protected boolean isRegister;
    protected abstract IntentFilter getIntentFilter();

    public Map<Context, L> getListeners(){
        StaticHandler<BaseReceiver> staticHandler = staticHandlerHashMap.get(getClass());
        if(staticHandler == null){
            staticHandler = new StaticHandler<>();
            staticHandlerHashMap.put(getClass(), staticHandler);
        }
        return (Map<Context, L>)staticHandler.listeners;
    }

    protected static <T extends BaseReceiver> T getInstance(Class<T> clazz){
        StaticHandler<T> staticHandler = (StaticHandler<T>) staticHandlerHashMap.get(clazz);
        if(staticHandler == null){
            return null;
        }
        if(staticHandler.hardReference != null){
            return staticHandler.hardReference;
        }
        T obj;
        if(staticHandler.weakReference != null && (obj = staticHandler.weakReference.get()) != null){
            return obj;
        }
        return null;
    }
}
