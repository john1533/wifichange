package me.fourtween.wifichange;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void saveContent(View view){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("me.fourtween.wifichange","me.fourtween.wifichange.MyService"));
        this.startService(intent);
    }
    public void checkConnection(View view){
        Log.v("wifichange","isNetworkAvailable:"+isNetworkAvailable(this));
        Log.v("wifichange","ping check:"+ping());
    }

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if(connectivity == null){
            return false;
        }else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if(info != null ){
                for(int i=0; i<info.length; i++){
                    if(info[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
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
            Log.i("TTT", "result content : " + stringBuffer.toString());
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
            Log.i("TTT", "result = " + result);
        }
        return false;
    }


}
