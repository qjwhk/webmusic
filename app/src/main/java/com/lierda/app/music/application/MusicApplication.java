package com.lierda.app.music.application;

import android.app.Application;

//import com.tencent.bugly.Bugly;
import com.lierda.app.music.http.HttpInterceptor;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 自定义Application
 * Created by wcy on 2015/11/27.
 */
public class MusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCache.init(this);
        ForegroundObserver.init(this);
        initOkHttpUtils();
//        initBugly();
    }

    private void initOkHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpInterceptor())
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

//    private void initBugly() {
//        if (!BuildConfig.DEBUG) {
//            Bugly.init(this, BuildConfig.BUGLY_APP_ID, false);
//        }
//    }
}
