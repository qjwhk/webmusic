package com.lierda.app.music.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lierda.app.music.activity.MainActivity;
import com.lierda.app.music.R;
import com.lierda.app.music.adapter.PlaylistAdapter;
import com.lierda.app.music.application.AppCache;
import com.lierda.app.music.constants.Keys;
import com.lierda.app.music.model.SongListInfo;
import com.lierda.app.music.utils.binding.Bind;

import java.util.List;
/**
 * 在线音乐
 * Created by wcy on 2015/11/26.
 */
public class PlaylistFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    @Bind(R.id.lv_playlist)
    private ListView lvPlaylist;
    public static int requestCode = 0;
    private List<SongListInfo> mSongLists;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSongLists = AppCache.getSongListInfos();
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
        PlaylistAdapter adapter = new PlaylistAdapter(mSongLists);
        lvPlaylist.setAdapter(adapter);
    }

    @Override
    protected void setListener() {
        lvPlaylist.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MainActivity activity= (MainActivity) getActivity();
        OnlineMusicFragment onlineMusicFragment=new OnlineMusicFragment();
        SongListInfo songListInfo = mSongLists.get(position);
        onlineMusicFragment.setListInfo(songListInfo);
        onlineMusicFragment.setOffset();
        activity.setLeftFragment(onlineMusicFragment);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        int position = lvPlaylist.getFirstVisiblePosition();
        int offset = (lvPlaylist.getChildAt(0) == null) ? 0 : lvPlaylist.getChildAt(0).getTop();
        outState.putInt(Keys.PLAYLIST_POSITION, position);
        outState.putInt(Keys.PLAYLIST_OFFSET, offset);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        lvPlaylist.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(Keys.PLAYLIST_POSITION);
                int offset = savedInstanceState.getInt(Keys.PLAYLIST_OFFSET);
                lvPlaylist.setSelectionFromTop(position, offset);
            }
        });
    }
}
