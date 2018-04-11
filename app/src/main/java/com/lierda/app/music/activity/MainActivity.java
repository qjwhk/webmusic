package com.lierda.app.music.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lierda.app.music.R;
import com.lierda.app.music.application.AppCache;
import com.lierda.app.music.executor.PlayOnlineMusic;
import com.lierda.app.music.fragment.BaseFragment;
import com.lierda.app.music.fragment.MainFragment;
import com.lierda.app.music.fragment.MusicFragment;
import com.lierda.app.music.fragment.OnlineMusicFragment;
import com.lierda.app.music.fragment.PlayFragment;
import com.lierda.app.music.fragment.PlaylistFragment;
import com.lierda.app.music.http.HttpCallback;
import com.lierda.app.music.http.HttpClient;
import com.lierda.app.music.model.DeviceInfo;
import com.lierda.app.music.model.Music;
import com.lierda.app.music.model.MusicControl;
import com.lierda.app.music.model.OnlineMusic;
import com.lierda.app.music.model.OnlineMusicList;
import com.lierda.app.music.model.SongListInfo;
import com.lierda.app.music.model.Splash;
import com.lierda.app.music.service.PlayService;
import com.lierda.app.music.utils.binding.ViewBinder;
import com.lierda.app.music.utils.proguard.FileUtils;
import com.lierda.app.music.utils.proguard.PermissionReq;
import com.lierda.app.music.utils.proguard.Preferences;
import com.lierda.app.music.utils.proguard.ToastUtils;
import com.lierda.app.music.utils.rabbitMq.MacAddr;
import com.lierda.app.music.utils.rabbitMq.Msg;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import static com.lierda.app.music.utils.proguard.MusicUtils.scanMusic;

public class MainActivity extends AppCompatActivity{
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private static final String SPLASH_FILE_NAME = "splash";
    public FragmentManager fm;
    public android.app.FragmentTransaction ft;
    private ServiceConnection mPlayServiceConnection;
    public MainFragment mainFragment=null;
    public PlayFragment playFragment=null;
    public MusicFragment musicFragment=null;
    boolean mainAdded=false;
    boolean musicAdded=false;
    private String sourceId;
    private String macAddr;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(setHome());
        setStatusBar();
        ViewBinder.bind(this);
        checkService();
        initFragment();
        addFragment("main",playFragment);
        getMsg(incomingMessageHandler);
    }

    /**
     * 状态栏
     */
    private void setStatusBar(){
        Window window = this.getWindow();
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
//        window.setStatusBarColor(getResources().getColor(R.color.statusBarColor));
        //设置系统状态栏处于可见状态
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private View setHome(){
        View main=getLayoutInflater().inflate(R.layout.activity_main2, null);
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  |  View.SYSTEM_UI_FLAG_IMMERSIVE);
        return main;
    }

    public void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, PlayService.class);
        stopService(intent);
    }

    final Handler incomingMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("msg");
            JSONObject jsonObject=JSON.parseObject(message);
            String sth=JSON.parseObject(jsonObject.getString("attributes")).getString("SWI");
            if(sth.equals("2")){
                AppCache.getPlayService().pause();
                sendMsg(userId,"2");
            }else if(sth.equals("1")){
                addFragment("music",null);
                AppCache.getPlayService().start();
                sendMsg(userId,"1");
            }else if(sth.equals("0")){
                addFragment("main",null);
            }
        }
    };

    public void sendMsg(String userid, String state){
        MusicControl musicControl=new MusicControl();
        musicControl.setSourceId(sourceId);
        musicControl.setRequestType("heart");
        musicControl.setId(macAddr);
        musicControl.setAttributes(new DeviceInfo(userid,state,"TC-PC"));
        final String music= JSON.toJSONString(musicControl);
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new Msg().sendMsg(music,"*."+sourceId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }


    public  Handler getHandler(){
        return incomingMessageHandler;
    }

    private Msg m=new Msg();
    private int f=0;
    private Thread t;
    public void getMsg(Handler handler){
        if(t!=null){
            t.interrupt();
        }
        t=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(sourceId!=null) {
                        m.recvMsg(macAddr + "." + getSourceId(), incomingMessageHandler);
                    }
                } catch (Exception e) {

                }
            }
        });
        t.start();
    }



    public void setSourceId(String sourceId){
        this.sourceId=sourceId;
    }

    public String getSourceId(){
        return this.sourceId;
    }

    public void setUserId(String userId){
        this.userId=userId;
    }

    public void setMacAddr(String macAddr){
        this.macAddr=macAddr;
    }

    public void addFragment(String tag, Fragment fragment){
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(tag.equals("main")){
            transaction.hide(musicFragment);
            if(!mainAdded){
                transaction.add(R.id.f_btn, mainFragment);
                mainAdded=true;
            }
            transaction.show(mainFragment);
        }else if(tag.equals("music")){
            transaction.hide(mainFragment);
            if(!musicAdded){
                transaction.add(R.id.f_btn, musicFragment);
                musicAdded=true;
            }
            transaction.show(musicFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    public void initFragment(){
        mainFragment=new MainFragment();
        playFragment=new PlayFragment();
        musicFragment=new MusicFragment();
    }

    public void hideFragment(Fragment fragment){
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragment);
    }

    public void addMusicFragment(){
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        PlaylistFragment playlistFragment=new PlaylistFragment();
        OnlineMusicFragment onlineMusicFragment=new OnlineMusicFragment();
        transaction.replace(R.id.music_type, playlistFragment,"playlistFragment");
        transaction.addToBackStack("playlistFragment");
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
        setDefMusic();
    }


    public void setFragment(){
        OnlineMusicFragment onlineMusicFragment=new OnlineMusicFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.music_play, onlineMusicFragment,"onlineMusicFragment").commit();
    }

    public void setLeftFragment(BaseFragment index){
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(index==null){
            PlaylistFragment playlistFragment=new PlaylistFragment();
            transaction.add(R.id.music_type, playlistFragment,"playlistFragment");
            transaction.addToBackStack("playlistFragment");
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.commit();
        }else if(index.getClass().equals(OnlineMusicFragment.class)){
            getSupportFragmentManager().beginTransaction().replace(R.id.music_play, (OnlineMusicFragment)index).commit();
        }else if(index.getClass().equals(PlayFragment.class)){
            getSupportFragmentManager().beginTransaction().add(R.id.music_list, (PlayFragment)index).commit();

        }
    }

    boolean isDef=true;
    public  void setDefMusic(){
        if(music!=null){
            return;
        }
        List<SongListInfo> mSongLists= AppCache.getSongListInfos();
        if (mSongLists.isEmpty()) {
            String[] titles = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SongListInfo info = new SongListInfo();
                info.setTitle(titles[i]);
                info.setType(types[i]);
                mSongLists.add(info);
            }
        }

        OnlineMusicFragment onlineMusicFragment=new OnlineMusicFragment();
        onlineMusicFragment.setListInfo(mSongLists.get(1));
        onlineMusicFragment.setOffset();
        this.setLeftFragment(onlineMusicFragment);
        SongListInfo info = new SongListInfo();
        info=mSongLists.get(1);
        info.setType(String.valueOf(2));
        getMusic(info);
        isDef=false;
    }


    public void setPlayingMusic(Music music){
        this.music=music;
    }

    private void getMusic(SongListInfo mListInfo) {
        HttpClient.getSongListInfo(mListInfo.getType(), 1, 0, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList response) {
                play(response.getSong_list().get(0));
            }

            @Override
            public void onFail(Exception e) {
                if (e instanceof RuntimeException) {
                    // 歌曲全部加载完成
                    return;
                }
            }
        });
    }


    public Music music;
    private void play(OnlineMusic onlineMusic) {
        new PlayOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {

            }

            @Override
            public void onExecuteSuccess(Music music) {
                getPlayService().play(music);
                MainActivity.this.setLeftFragment(new PlayFragment());
                ToastUtils.show(getString(R.string.now_play, music.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                ToastUtils.show(R.string.unable_to_play);
            }
        }.execute();
    }

    public PlayService getPlayService() {
        PlayService playService = AppCache.getPlayService();
        if (playService == null) {
            throw new NullPointerException("play service is null");
        }
        return playService;
    }


    private void checkService() {
        if (AppCache.getPlayService() == null) {
            startService();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindService();
                }
            }, 1000);
        } else {
//            finish();
        }
    }

    private void startService() {
        Intent intent = new Intent(this, PlayService.class);
        startService(intent);
    }

    private void showSplash() {
        File splashImg = new File(FileUtils.getSplashDir(this), SPLASH_FILE_NAME);
        if (splashImg.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(splashImg.getPath());
        }
    }

    private void updateSplash() {
        HttpClient.getSplash(new HttpCallback<Splash>() {
            @Override
            public void onSuccess(Splash response) {
                if (response == null || TextUtils.isEmpty(response.getUrl())) {
                    return;
                }

                final String url = response.getUrl();
                String lastImgUrl = Preferences.getSplashUrl();
                if (TextUtils.equals(lastImgUrl, url)) {
                    return;
                }

                HttpClient.downloadFile(url, FileUtils.getSplashDir(AppCache.getContext()), SPLASH_FILE_NAME,
                        new HttpCallback<File>() {
                            @Override
                            public void onSuccess(File file) {
                                Preferences.saveSplashUrl(url);
                            }

                            @Override
                            public void onFail(Exception e) {
                            }
                        });
            }

            @Override
            public void onFail(Exception e) {
            }
        });
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, PlayService.class);
        mPlayServiceConnection = new MainActivity.PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }


    private class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final PlayService playService = ((PlayService.PlayBinder) service).getService();
            AppCache.setPlayService(playService);
            PermissionReq.with(MainActivity.this)
                    .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .result(new PermissionReq.Result() {
                        @Override
                        public void onGranted() {
                            scanMusic(playService);
                        }

                        @Override
                        public void onDenied() {
                            ToastUtils.show(R.string.no_permission_storage);
//                            finish();
                            playService.quit();
                        }
                    })
                    .request();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    @Override
    public void onBackPressed() {
        addFragment("main",musicFragment);
    }
}
