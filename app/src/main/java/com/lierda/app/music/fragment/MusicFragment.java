package com.lierda.app.music.fragment;

import android.Manifest;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.aps.ac;
import com.lierda.app.music.R;
import com.lierda.app.music.activity.MainActivity;
import com.lierda.app.music.application.AppCache;
import com.lierda.app.music.executor.PlayOnlineMusic;
import com.lierda.app.music.http.HttpCallback;
import com.lierda.app.music.http.HttpClient;
import com.lierda.app.music.model.Music;
import com.lierda.app.music.model.OnlineMusic;
import com.lierda.app.music.model.OnlineMusicList;
import com.lierda.app.music.model.SongListInfo;
import com.lierda.app.music.model.Splash;
import com.lierda.app.music.service.PlayService;
import com.lierda.app.music.utils.binding.Bind;
import com.lierda.app.music.utils.binding.ViewBinder;
import com.lierda.app.music.utils.proguard.FileUtils;
import com.lierda.app.music.utils.proguard.PermissionReq;
import com.lierda.app.music.utils.proguard.Preferences;
import com.lierda.app.music.utils.proguard.ToastUtils;

import java.io.File;
import java.util.List;

import static com.lierda.app.music.utils.proguard.MusicUtils.scanMusic;

/**
 * Created by Administrator on 2017/11/20.
 */

public class MusicFragment extends  BaseFragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_music, container, false);
        MainActivity activity= (MainActivity) getActivity();
        activity.setLeftFragment(null);
        activity.setDefMusic();
        return view;
    }
}


