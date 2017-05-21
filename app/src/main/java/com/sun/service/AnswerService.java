package com.sun.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sun.connect.EventNetwork;
import com.sun.connect.NetworkChannel;
import com.sun.device.AskNote;
import com.sun.device.NoteHelper;
import com.sun.level.OrderNote;
import com.sun.level.UpdateOrderNote;
import com.sun.personalconnect.Application;
import com.sun.utils.FormatUtils;
import com.sun.utils.ToastUtils;
import com.sun.utils.Utils;

import java.io.File;

/**
 * Created by sun on 2017/5/21.
 */

public class AnswerService extends Service implements NetworkChannel.INetworkListener{
    private ServiceBinder mBinder;
    private NetworkChannel mNetworkChannel;
    public static final String TAG = "AnswerService";
    private int mDownloadCode = 0;
    @Override
    public void onEventNetwork(EventNetwork eventNetwork) {
        if(eventNetwork.isMine()){
            if(!TextUtils.isEmpty(eventNetwork.getError())){
                ToastUtils.show("请求错误 error:" + eventNetwork.getError() , Toast.LENGTH_SHORT);
                Log.e(TAG, String.format("请求错误 error:%s,step:%d",eventNetwork.getError(),eventNetwork.getStep()));
            }else{
                if(eventNetwork.getObject() instanceof File){
                    checkInstallApk(mDownloadCode, (File)eventNetwork.getObject(),false);
                }
                Log.d(TAG, "请求设备信息成功");
            }
            return;
        }

        Object obj = eventNetwork.getObject();
        if(obj instanceof AskNote){
            if(TextUtils.isEmpty(eventNetwork.getError())){
                AskNote note = ((AskNote) eventNetwork.getObject());
                mNetworkChannel.request(FormatUtils.makeRequest(null, NoteHelper.makeAnswer(note)));
            }
        }else if(obj instanceof OrderNote){
            if(obj instanceof UpdateOrderNote){
                if (TextUtils.isEmpty(((UpdateOrderNote) obj).getApkName())){
                    return;
                }
                if(((UpdateOrderNote) obj).isForce() || Application.App.VersionCode < ((UpdateOrderNote) obj).getVersionCode()){
                    File apkFile = new File(Application.App.getSocketRawFolder(), ((UpdateOrderNote) obj).getApkName());
                    checkInstallApk(((UpdateOrderNote) obj).getVersionCode(), apkFile,true);
                }
            }
        }
    }

    private void checkInstallApk(int code, File apkFile, boolean retryDownload){
        PackageInfo pkgInfo = null;
        if(apkFile.exists()){
            PackageManager pm = getPackageManager();
            try {
                pkgInfo = pm.getPackageArchiveInfo(apkFile.getPath(), PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
            } catch (Exception e) {
                // should be something wrong with parse
                e.printStackTrace();
            }
            if(pkgInfo == null || pkgInfo.versionCode != code){
                apkFile.delete();
                pkgInfo = null;
            }
        }
        if(pkgInfo == null){
            if(retryDownload) {
                mNetworkChannel.download(FormatUtils.makeDownloadRequest(null,apkFile.getName()));
                mDownloadCode = code;
            }
        }else{
            install(apkFile);
        }
    }

    private void install(File apkFile){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public class ServiceBinder extends Binder {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNetworkChannel = new NetworkChannel();
        mNetworkChannel.init(this);
        mNetworkChannel.setNetworkListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        return Service. START_STICKY;
    }

    @Override
    public void onDestroy() {
        mNetworkChannel.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
