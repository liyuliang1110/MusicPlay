package com.example.blue.musicplay.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.adapter.MusicAdapter;
import com.example.blue.musicplay.basic.AcitivtyList;
import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.engin.MusicScan;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.example.blue.musicplay.pool.ObjectPool;

import java.util.List;

public class LocalMusicListActivity extends AppCompatActivity {
    @ViewInject(R.id.music_list)
    private ListView listView;
    private CircularMusicProgressBar progressBar;
    @ViewInject(R.id.local_music_back)
    private ImageView local_music_back;
    private static final String TAG = "LocalMusicActivity --->";
    public static final String ListMusicInfo_String = "ListMp3Info";
    public static final String Now_Mp3Info = "Now_Mp3Info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_musci_list);
        ViewUtils.inject(this);
        init();
        final List<Mp3Info> listMusic = MusicScan.getMusicData(getApplicationContext()); //获取音乐列表
        ObjectPool.getInstance().creatObject(ListMusicInfo_String, listMusic); //将List音乐列表对象存入对象池中
        MusicAdapter adapter = new MusicAdapter(this, listMusic);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long l) {
                Intent intent = new Intent(LocalMusicListActivity.this, MusicPlayerActivity.class);
                ObjectPool.getInstance().creatObject(MusicPlayerActivity.NOW_Mp3Info_String,listMusic.get(postion));
                startActivity(intent);
            }
        });
    }

    private void init() {
        AcitivtyList.getSingInstance().getList().add(this);
    }

    @OnClick(R.id.local_music_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.local_music_back:
                LocalMusicListActivity.this.finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LocalMusicListActivity.this.finish();
        }
        return false;
    }

}
