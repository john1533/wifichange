package me.fourtween.wifichange;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by John on 2018/9/27.
 */

public class MyService extends Service{
    private static final String FILE = "config";
    private static final String CON = "connected";
    private boolean running = false;
    private static int TIME_OUT = 15;
    private static final String TAG = "wifichange";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG,"instance:"+this+"----running:"+running);
        if(running)
            return super.onStartCommand(intent, flags, startId);
        else{
            new Thread(){
                @Override
                public void run() {
                    connect(false);
                }
            }.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void changeWifi(Context context, String ssid) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        int targetNetId = -1;
        Log.v(TAG,"starting...");
        for (WifiConfiguration wifiConfiguration : list) {
            String wifiSSID = wifiConfiguration.SSID;
            if (ssid.equals(wifiSSID)||("\"" + ssid + "\"").equals(wifiSSID)) {
                Log.v(TAG,"target:"+wifiSSID);
                targetNetId = wifiConfiguration.networkId;
            }else{
                wifiManager.disableNetwork(wifiConfiguration.networkId);
            }
        }
        if(targetNetId != -1){
            Log.v(TAG,"enableNetwork result:"+wifiManager.enableNetwork(targetNetId, true));
        }

    }

    private Set<String> getAllSSid(){
        WifiManager wifiManager = (WifiManager)getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        Set<String> set = new TreeSet<>();
        for (WifiConfiguration wifiConfiguration : list) {
            Log.v(TAG,"wifiSSID:"+wifiConfiguration.SSID);
            set.add(wifiConfiguration.SSID);
        }
        return set;
    }

    private String getCurSSID(){
        WifiManager wifiManager = (WifiManager) getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    private void connect(boolean must){
        running = true;
        if(!must && ping()){
            SharedPreferences shred = this.getSharedPreferences(FILE,MODE_PRIVATE);
            Set<String> set = shred.getStringSet(CON,null);
            Set<String> treeSet = new TreeSet<>();
            if(set != null && set.size()>0){
                treeSet.addAll(set);
            }
            String curSSID = getCurSSID();
            if(!treeSet.contains(curSSID)){
                treeSet.add(curSSID);
            }
            SharedPreferences.Editor editor = shred.edit();
            editor.putStringSet(CON,treeSet);
            editor.commit();
            running = false;
            return;
        }

        SharedPreferences shred = this.getSharedPreferences(FILE,MODE_PRIVATE);
        Set<String> set = shred.getStringSet(CON,null);
        Set<String> treeSet = new TreeSet<>();
        if(set !=null && set.size()>0){
            treeSet.addAll(set);
            for(String ssid:treeSet){
                Log.v(TAG,"config.xml ssid:"+ssid);
                int count = 0;
                changeWifi(this,ssid);
                while (count<TIME_OUT){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(ping()){
                        running = false;
                        return;
                    }
                    count++;
                }
                treeSet.remove(ssid);
            }
        }

        Set<String> allSSids = getAllSSid();
        for(String ssid:allSSids){
            if(treeSet.contains(ssid)){
                continue;
            }
            int count = 0;
            changeWifi(this,ssid);
            while (count<TIME_OUT){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(ping()){
                    Log.v(TAG,"connected--"+ssid);
                    if(!treeSet.contains(ssid)){
                        treeSet.add(ssid);
                    }
                    SharedPreferences.Editor editor = shred.edit();
                    editor.putStringSet(CON,treeSet);
                    editor.commit();
                    Log.v(TAG,"editor addddd--"+ssid);
                    running = false;
                    return;
                }
                count++;
            }
        }

        SharedPreferences.Editor editor = shred.edit();
        editor.putStringSet(CON,set);
        editor.commit();
        running = false;
//        connect(false);
    }

    public final boolean ping() {
        String urld = "www.baidu.com";
        String result = null;
        try {
            String ip = urld;// 除非百度挂了，否则用这个应该没问题(也可以换成自己要连接的服务器地址)
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 1 " + ip);// ping3次
            // 读取ping的内容，可不加。
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.i(TAG, "result content : " + stringBuffer.toString());
            // PING的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                return true;
            } else {
                result = "failed~ cannot reach the IP address";
                return false;
            }
        } catch (IOException e) {
            result = "failed~ IOException";
        } catch (InterruptedException e) {
            result = "failed~ InterruptedException";
        } finally {
            Log.i(TAG, "result = " + result);
        }
        return false;
    }
}
