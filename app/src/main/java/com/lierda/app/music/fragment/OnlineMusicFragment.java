package com.lierda.app.music.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;

import com.lierda.app.music.activity.MainActivity;
import com.lierda.app.music.R;
import com.lierda.app.music.adapter.OnlineMusicAdapter;
import com.lierda.app.music.application.AppCache;
import com.lierda.app.music.executor.PlayOnlineMusic;
import com.lierda.app.music.http.HttpCallback;
import com.lierda.app.music.http.HttpClient;
import com.lierda.app.music.model.Music;
import com.lierda.app.music.model.OnlineMusic;
import com.lierda.app.music.model.OnlineMusicList;
import com.lierda.app.music.model.SongListInfo;
import com.lierda.app.music.service.PlayService;
import com.lierda.app.music.utils.proguard.ScreenUtils;
import com.lierda.app.music.utils.proguard.ToastUtils;
import com.lierda.app.music.widget.AutoLoadListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/8.
 */

public class OnlineMusicFragment extends  BaseFragment implements AdapterView.OnItemClickListener,AutoLoadListView.OnLoadListener{
    private AutoLoadListView lvOnlineMusic;
    private Button backBtn;
    public static int requestCode = 1;
    private static final int MUSIC_LIST_SIZE = 20;
    private SongListInfo mListInfo;
    private OnlineMusicList mOnlineMusicList;
    private int mOffset = 0;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private OnlineMusicAdapter mAdapter = new OnlineMusicAdapter(mMusicList);
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_onlinemusic, container, false);
        lvOnlineMusic= (AutoLoadListView) view.findViewById(R.id.lv_online_music_list);
        backBtn= (Button) view.findViewById(R.id.btn_back);
        setBtnClick();
        init();
        onLoad();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        play((OnlineMusic) parent.getAdapter().getItem(position));
    }

    protected void setListener(){
        lvOnlineMusic.setOnItemClickListener(this);
    }


    private void play(OnlineMusic onlineMusic) {
        new PlayOnlineMusic(getActivity(), onlineMusic) {
            @Override
            public void onPrepare() {

            }

            @Override
            public void onExecuteSuccess(Music music) {
                getPlayService().play(music);
                MainActivity mainActivity= (MainActivity) getActivity();
                mainActivity.setLeftFragment(new PlayFragment());
                mainActivity.setPlayingMusic(music);
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

    @Override
    public void onLoad() {
        getMusic(mOffset);
    }

    public void reload(){
        init();
        onLoad();
    }

    public void setOffset(){
        this.mOffset=0;
    }

    public void setListInfo(SongListInfo mListInfo){
        this.mListInfo=mListInfo;
    }

    private void setBtnClick(){
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity=(MainActivity)getActivity();
                activity.setLeftFragment(null);
            }
        });
    }
    private void init() {
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(150));
        lvOnlineMusic.setAdapter(mAdapter);
        lvOnlineMusic.setOnLoadListener(this);
    }


    private  void getMusic(final int offset) {
        HttpClient.getSongListInfo(mListInfo.getType(), MUSIC_LIST_SIZE, offset, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList response) {
                lvOnlineMusic.onLoadComplete();
                mOnlineMusicList = response;
                if (response == null || response.getSong_list() == null || response.getSong_list().size() == 0) {
                    lvOnlineMusic.setEnable(false);
                    return;
                }
                mOffset += MUSIC_LIST_SIZE;
                mMusicList.addAll(response.getSong_list());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception e) {
                lvOnlineMusic.onLoadComplete();
                if (e instanceof RuntimeException) {
                    // 歌曲全部加载完成
                    lvOnlineMusic.setEnable(false);
                    return;
                }
            }
        });
    }
}
