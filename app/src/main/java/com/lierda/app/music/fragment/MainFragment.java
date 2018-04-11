package com.lierda.app.music.fragment;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aps.v;
import com.lierda.app.music.R;
import com.lierda.app.music.activity.MainActivity;
import com.lierda.app.music.application.AppCache;
import com.lierda.app.music.model.DeviceInfo;
import com.lierda.app.music.model.MusicControl;
import com.lierda.app.music.model.NetInfo;
import com.lierda.app.music.service.PlayService;
import com.lierda.app.music.utils.FileUtils;
import com.lierda.app.music.utils.binding.ViewBinder;
import com.lierda.app.music.utils.rabbitMq.MacAddr;
import com.lierda.app.music.utils.rabbitMq.Msg;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2017/11/20.
 */

public class MainFragment extends Fragment {
    public static String baseurl = "file:///android_asset/www/";
    public static WebView mWebView;
    public static String html;
    private String sourceId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_splash, container, false);
        initWeb(view);
        return view;
    }

    float mPosX,mPosY,mCurPosX,mCurPosY;
    /**初始化webview控件*/
    private  void initWeb(View view){
        mWebView = (WebView)(view.findViewById(R.id.main_web));
        setTouchListener(mWebView);
        html = FileUtils.readAssest(getActivity(), "www/index.html");
//        mWebView.getSettings().setJavaScriptEnabled(true);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportMultipleWindows(true);
        settings.setGeolocationEnabled(true);
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        switch (screenDensity) {
            case DisplayMetrics.DENSITY_LOW:
                zoomDensity = WebSettings.ZoomDensity.CLOSE;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                zoomDensity = WebSettings.ZoomDensity.FAR;
                break;
        }
        settings.setDefaultZoom(zoomDensity);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setInitialScale(25);
        mWebView.addJavascriptInterface(new JsInteration(), "jsbridge");
        webviewload();
    }
    private static void webviewload(){
        mWebView.loadDataWithBaseURL(baseurl, html, "text/html", "UTF-8", null);
    }
    private void setTouchListener(View view){
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        mPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = event.getX();
                        mCurPosY = event.getY();

                        break;
                    case MotionEvent.ACTION_UP:
                        if (mCurPosY - mPosY > 0
                                && (Math.abs(mCurPosY - mPosY) > 45)) {
                            //向下滑動
                            ((MainActivity)getActivity()).hideNavigationBar();
                        }
//                        else if (mCurPosY - mPosY < 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {
//                            //向上滑动
//
//                        }

                        break;
                }
                return false;
            }
        });
    }


    public class JsInteration {
        @JavascriptInterface
        public void music() {
            MainActivity activity = (MainActivity) getActivity();
            activity.addFragment("music",MainFragment.this);
        }
        @JavascriptInterface
        public void pushUserSever(String userid,String source){
            sourceId=source;
            sendMsg(source,userid);
            MainActivity mainActivity= (MainActivity) getActivity();
            Handler handler = mainActivity.getHandler();
            mainActivity.setSourceId(sourceId);
            mainActivity.setUserId(userid);
            mainActivity.getMsg(handler);
        }
        @JavascriptInterface
        public void back(){

        }
        /**
         *网络连接判断
         */
        @JavascriptInterface
        public void  queryWifiState(){
            NetInfo netInfo = getNetworkInfo();
        }
    }

    private NetInfo getNetworkInfo(){
        final NetInfo netInfo=new NetInfo();
        Context context=getActivity();
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null&&mNetworkInfo.isAvailable()) {
                netInfo.setStatus("1");
                NetworkInfo info=mConnectivityManager.getActiveNetworkInfo();
                if (info!=null&&info.isAvailable()&&info.getType()==ConnectivityManager.TYPE_WIFI){
                    netInfo.setType("1");
                }else {
                    netInfo.setType("0");
                }
            }else {
                netInfo.setStatus("0");
            }
        }
        Toast.makeText(getActivity(), JSON.toJSONString(netInfo), Toast.LENGTH_SHORT).show();

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:getWifiState(\""+netInfo.getStatus()+"\",\""+netInfo.getType()+"\")");
            }
        });
        return netInfo;
    }


    public void sendMsg(String source,String userid){
        String macAddrr=getMacAddress();
        MusicControl musicControl=new MusicControl();
        musicControl.setSourceId(source);
        musicControl.setRequestType("heart");
        musicControl.setId(macAddrr);
        musicControl.setAttributes(new DeviceInfo(userid,"1","TC-PC"));
        String music= JSON.toJSONString(musicControl);
        ((MainActivity)getActivity()).setMacAddr(macAddrr);
        try {
            new Msg().sendMsg(music,"*."+source);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalMacAddressFromWifiInfo(Context context){
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    public String getMacAddress(){
        String macAddr="";
        final WifiManager wm = (WifiManager) getActivity().getApplicationContext() .getSystemService(Service.WIFI_SERVICE);
        WifiInfo info=wm.getConnectionInfo();
        macAddr=info.getMacAddress().toUpperCase().replace(":","");
        return  macAddr;
    }
}
