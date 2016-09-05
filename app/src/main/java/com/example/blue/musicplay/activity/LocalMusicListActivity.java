package com.example.blue.musicplay.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.adapter.MusicAdapter;
import com.example.blue.musicplay.basic.AcitivtyList;
import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.pool.ObjectPool;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
    private static List<Mp3Info> listMusic;
    private static MusicAdapter adapter = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_musci_list);
        ViewUtils.inject(this);
        init();
        listMusic = (List<Mp3Info>) ObjectPool.getInstance().getObject(ListMusicInfo_String);
        adapter = new MusicAdapter(this, listMusic);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long l) {
                File file = new File(listMusic.get(postion).getUrl());
                if (!file.exists()) {
                    Toast.makeText(LocalMusicListActivity.this, "您点击的音乐已经从手机中移除", Toast.LENGTH_SHORT).show();
                    listMusic.remove(postion);
                    adapter.notifyDataSetChanged();
                    saveMusicList(listMusic); //更新数据
                } else {
                    Intent intent = new Intent(LocalMusicListActivity.this, MusicPlayerActivity.class);
                    Object now_uri = ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_Mp3Info_String);
                    if (now_uri == listMusic.get(postion).getUrl())
                        intent.putExtra("flag", true);
                    ObjectPool.getInstance().creatObject(MusicPlayerActivity.NOW_Mp3Info_String, listMusic.get(postion));
                    startActivity(intent);

                }
            }
        });
    }

    private void saveMusicList(List<Mp3Info> list) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(list);
            String base64 = new String(Base64.encodeBase64(outputStream.toByteArray()));
            SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
            editor.putString("music_list", base64);
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
