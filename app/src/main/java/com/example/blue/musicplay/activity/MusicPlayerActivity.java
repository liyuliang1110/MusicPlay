package com.example.blue.musicplay.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.Service.MusicService;
import com.example.blue.musicplay.basic.AcitivtyList;
import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.pool.ObjectPool;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 * Created by blue on 2016/8/14.
 */

public class MusicPlayerActivity extends AppCompatActivity {
    private static final String TAG = "MusicPlayerActivity --->";
    public  static final String ACTION_UPDATEUI = "action.updateUI";
    public  static final String RECEIOVERACTION = "MusicControl";
    public  static final String NOW_musicUri_String = "now_musicUri";
    public  static final String NOW_Mp3Info_String = "NOW_Mp3Info";
    public  static final String ACTION_FLUSH_PROGRESSBAR = "flushProgressBar";
    public  static final String ACTION_BTNUPDATE_PAUSE = "flush_btn_pause";
    public  static final String ACTION_BTUPNDATA_PLAY = "flush_btn_play";
    @ViewInject(R.id.progressBar)
    private CircularMusicProgressBar progressBar;
    @ViewInject(R.id.music_play_musicName)
    private TextView music_play_musicName;
    @ViewInject(R.id.music_play_songName)
    private TextView music_play_songName;
    @ViewInject(R.id.music_play_play)
    private ImageView music_play;
    @ViewInject(R.id.music_play_next)
    private ImageView music_play_next;
    @ViewInject(R.id.music_play_previous)
    private ImageView music_play_last;
    private Mp3Info Now_Mp3Info = null;
    private String uri = null;
    /*处理音乐播放界面的Handler */
    private Handler uiHandler;
    /* 控制音乐播放*/
    private MediaPlayer player;
    /*Service通过广播刷新UI*/
    UpdateUIBroadcastReceiver broadcastReceiver;
    private int current = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_palyer_activity);
        ViewUtils.inject(this);
        init();
        progressBar.setValue(1);
    }

    private void init() {
        AcitivtyList.getSingInstance().getList().add(this);
        initMusicPlayerLayout();
        broadcastReceiver = new UpdateUIBroadcastReceiver();
        IntentFilter filter = new IntentFilter();//过滤广播
        filter.addAction(ACTION_UPDATEUI);
        broadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);//注册接收广播
        handlerClick();
        player = (MediaPlayer) ObjectPool.getInstance().getObject("MediaPlayer");
        ObjectPool.getInstance().creatObject(NOW_musicUri_String, Now_Mp3Info.getUrl());
        ObjectPool.getInstance().creatObject("uiHandler", uiHandler);
        Intent intent = new Intent();
        intent.setAction(MusicService.ACTION_ServiceConnection);
        intent.putExtra(RECEIOVERACTION, "play");
        sendBroadcast(intent);
        Log.d(TAG, "正在播放" + intent.getStringExtra("Music_Name"));
    }
    private void initMusicPlayerLayout() {
        Now_Mp3Info = (Mp3Info) ObjectPool.getInstance().getObject(NOW_Mp3Info_String);
        music_play_musicName.setText(Now_Mp3Info.getName());
        music_play_songName.setText(Now_Mp3Info.getSinger());
        Log.d(TAG, "initMusicPlayerLayout: "+ Now_Mp3Info.getName());
        if (Now_Mp3Info.getImagePath()!=null) {
            Bitmap bm = BitmapFactory.decodeFile(Now_Mp3Info.getImagePath());
            progressBar.setImageBitmap(bm);
        }
    }

    private void handlerClick() {
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what <= 100) {
                    progressBar.setValue(msg.what);
                }else if(msg.what == 200) {
                    Mp3Info Now_Mp3Info = (Mp3Info) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_musicUri_String);
                    if (Now_Mp3Info != null) {
                        progressBar.setValue(1);
                        Log.d(TAG, "onReceive: " + Now_Mp3Info.getName());
                        music_play_musicName.setText(Now_Mp3Info.getName());
                        music_play_songName.setText(Now_Mp3Info.getSinger());
                    }
                }
            }
        };
    }

    @OnClick({R.id.music_play_play, R.id.music_play_next, R.id.music_play_previous})
    private void musicPlayerControl(View view) {
        Intent musicControlIntent = new Intent();
        musicControlIntent.setAction(MusicService.ACTION_ServiceConnection);
        Log.d(TAG, "点击暂停或者播放");
        switch (view.getId()) {
            case R.id.music_play_play:
                MediaPlayer player = (MediaPlayer) ObjectPool.getInstance().getObject("MediaPlayer");
                if (player.isPlaying()) {
                    Log.d(TAG, "暂停");
                    musicControlIntent.putExtra(RECEIOVERACTION, "pause");
                    sendBroadcast(musicControlIntent);
                } else {
                    musicControlIntent.putExtra(RECEIOVERACTION, "continue");
                    sendBroadcast(musicControlIntent);
                }
                break;
            case R.id.music_play_previous:
                Log.d(TAG, "上一首");
                musicControlIntent.putExtra(RECEIOVERACTION, "previous");
                sendBroadcast(musicControlIntent);
                break;
            case R.id.music_play_next:
                Log.d(TAG, "下一首");
                musicControlIntent.putExtra(RECEIOVERACTION, "next");
                sendBroadcast(musicControlIntent);
                break;
        }
    }

    private class UpdateUIBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            Log.d(TAG, action);  //UI操作
            Now_Mp3Info = (Mp3Info) ObjectPool.getInstance().getObject(NOW_Mp3Info_String);
            if (Now_Mp3Info != null&&action.equals(ACTION_FLUSH_PROGRESSBAR)) {
                progressBar.setValue(1);
                Log.d(TAG, "onReceive: " + Now_Mp3Info.getName());
                initMusicPlayerLayout();
            }else if(Now_Mp3Info != null&&action.equals(ACTION_BTNUPDATE_PAUSE)) {
                music_play.setBackgroundResource(R.drawable.icon_music_play);
            }else if (Now_Mp3Info != null&&action.equals(ACTION_BTUPNDATA_PLAY)) {
                music_play.setBackgroundResource(R.drawable.icon_music_pause);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestory");
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MusicPlayerActivity.this.finish();
        }
        return false;
    }
}
