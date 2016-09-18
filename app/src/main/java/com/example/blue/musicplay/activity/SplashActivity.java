package com.example.blue.musicplay.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.basic.Utils;
import com.example.blue.musicplay.engin.MusicScan;
import com.example.blue.musicplay.pool.ObjectPool;

import java.util.List;

/**
 * Created by blue on 2016/9/16.
 */

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity--->";
    private List<Mp3Info> listMusic = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadMusicInfo();
        Intent intent = new Intent(this,DrawerLayoutActivity.class);
        startActivity(intent);
        this.finish();
    }
    private void loadMusicInfo() {
        SharedPreferences preferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        String musicList_string = preferences.getString("music_list",null);
        if (musicList_string==null||musicList_string=="") {
            listMusic = MusicScan.getMusicData(getApplicationContext()); //获取音乐列表
            Utils.getSingInstance().saveMusicList(listMusic,this);
            Log.d(TAG, "loadMusicInfo: 储存对象成功");
            ObjectPool.getInstance().creatObject(LocalMusicListActivity.ListMusicInfo_String,listMusic);
        }else {
            listMusic = Utils.getSingInstance().getMusicList(musicList_string);
            if (listMusic!=null) {
                ObjectPool.getInstance().creatObject(LocalMusicListActivity.ListMusicInfo_String, listMusic);
            }else {
                Toast.makeText(this,"读取音乐错误，请确认你的手机中是否有音乐",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
