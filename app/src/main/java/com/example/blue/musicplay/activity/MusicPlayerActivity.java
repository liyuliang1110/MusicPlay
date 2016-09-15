package com.example.blue.musicplay.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.Service.MusicService;
import com.example.blue.musicplay.basic.AcitivtyList;
import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.pool.ObjectPool;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.List;

/**
 * Created by blue on 2016/8/14.
 */

public class MusicPlayerActivity extends AppCompatActivity {
    private static final String TAG = "MusicPlayerActivity --->";
    public static final String ACTION_UPDATEUI = "action.updateUI";
    public static final String RECEIOVERACTION = "MusicControl";
    public static final String NOW_musicUri_String = "now_musicUri";
    public static final String NOW_Mp3Info_String = "NOW_Mp3Info";
    public static final String ACTION_FLUSH_PROGRESSBAR = "flushProgressBar";
    public static final String ACTION_BTNUPDATE_PAUSE = "flush_btn_pause";
    public static final String ACTION_BTUPNDATA_PLAY = "flush_btn_play";
    public static final String MusicPlayerContext = "MusicPlayContext";
    public static final String SeekBar_String = "SeekBar";
    @ViewInject(R.id.music_player_singImage)
    private ImageView music_player_singImage;
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
    @ViewInject(R.id.playSeekBar)
    private SeekBar seekBar;
    private Mp3Info Now_Mp3Info = null;
    private String uri = null;
    /* 控制音乐播放*/
    private MediaPlayer player;
    /*旋转动画*/
    private Animation rotation = null;
    /*Service通过广播刷新UI*/
    UpdateUIBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_palyer_activity);
        ViewUtils.inject(this);
        Log.d(TAG, "onCreate: 进入MusicPlayerActivity");
        init();
    }

    private void init() {
        ObjectPool.getInstance().creatObject(MusicPlayerContext,this);
        ObjectPool.getInstance().creatObject(SeekBar_String,seekBar);
        AcitivtyList.getSingInstance().getList().add(this);
        broadcastReceiver = new UpdateUIBroadcastReceiver();
        IntentFilter filter = new IntentFilter();//过滤广播
        filter.addAction(ACTION_UPDATEUI);
        broadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);//注册接收广播
        boolean flag = getIntent().getBooleanExtra("flag", false);
        if (flag&&isServiceWork(this,"com.example.blue.musicplay.Service.MusicService")) {
            Intent musicControlIntent = new Intent();
            musicControlIntent.setAction(MusicService.ACTION_ServiceConnection);
            musicControlIntent.putExtra(RECEIOVERACTION, "playingMusic");
            sendBroadcast(musicControlIntent);
        } else {
            initMusicService();
        }
        initMusicPlayerLayout();
    }
    private void initMusicService() {
        if (!isServiceWork(this, "com.example.blue.musicplay.Service.MusicService") || player != null) {
            Intent intent = new Intent(MusicPlayerActivity.this, MusicService.class);
            Log.d(TAG, "正在初始化MusicServic");
            startService(intent);
        }else {
            Log.d(TAG, "initMusicService: 播放音乐");
            Intent musicControlIntent = new Intent();
            musicControlIntent.setAction(MusicService.ACTION_ServiceConnection);
            musicControlIntent.putExtra(RECEIOVERACTION, "play");
            sendBroadcast(musicControlIntent);
        }
    }
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
    private void initMusicPlayerLayout() {
        Now_Mp3Info = (Mp3Info) ObjectPool.getInstance().getObject(NOW_Mp3Info_String);
        music_play_musicName.setText(Now_Mp3Info.getName());
        music_play_songName.setText(Now_Mp3Info.getSinger());
        seekBar.setProgress(0);
        Log.d(TAG, "initMusicPlayerLayout: " + Now_Mp3Info.getName());
        if (Now_Mp3Info.getImagePath() != null) {
            Bitmap bm = BitmapFactory.decodeFile(Now_Mp3Info.getImagePath());
            music_player_singImage.setImageBitmap(bm);
        }else {
            Resources resources =getResources();
            Bitmap bitmap = BitmapFactory.decodeResource(resources,R.drawable.icon_default_singer);
            music_player_singImage.setImageBitmap(bitmap);
        }
        rotation = AnimationUtils.loadAnimation(this,R.anim.music_player_rotation);
        LinearInterpolator lin = new LinearInterpolator();//匀速效果
        rotation.setInterpolator(lin);
        music_player_singImage.startAnimation(rotation);
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
                    music_player_singImage.clearAnimation();
                } else {
                    musicControlIntent.putExtra(RECEIOVERACTION, "continue");
                    sendBroadcast(musicControlIntent);
                    music_player_singImage.startAnimation(rotation);
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
            if (Now_Mp3Info != null && action.equals(ACTION_FLUSH_PROGRESSBAR)) {
                Log.d(TAG, "onReceive: " + Now_Mp3Info.getName());
                initMusicPlayerLayout();
            } else if (Now_Mp3Info != null && action.equals(ACTION_BTNUPDATE_PAUSE)) {
                music_play.setBackgroundResource(R.drawable.icon_music_play);
                music_player_singImage.clearAnimation();
            } else if (Now_Mp3Info != null && action.equals(ACTION_BTUPNDATA_PLAY)) {
                music_play.setBackgroundResource(R.drawable.icon_music_pause);
                music_player_singImage.startAnimation(rotation);
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
    class MusicControl implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            player.seekTo(seekBar.getProgress());
        }
    }
}
