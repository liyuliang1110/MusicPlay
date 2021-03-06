package com.example.blue.musicplay.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.activity.LocalMusicListActivity;
import com.example.blue.musicplay.activity.MusicPlayerActivity;
import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.basic.Utils;
import com.example.blue.musicplay.pool.ObjectPool;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by blue on 2016/8/10.
 */
public class MusicService extends Service implements ControlServic {
    /*与Service通信的action*/
    public static final String ACTION_ServiceConnection = "action.ServiceConnection";
    private static final String TAG = "MusicPlayerServic--->";
    private static final int btn_play = 1;
    private static final int btn_next = 2;
    private static final int btn_previous = 3;
    private static final String INTENT_BTNID = "INTENT_BTNID";
    /*默认为单曲循环*/
    private MusicPlayerType playerType = MusicPlayerType.cycle;
    private MediaPlayer player = new MediaPlayer();
    /*接收来自UI的广播*/
    public ServiceConnectionReceiver receiver;
    /*音乐信息列表*/
    private List<Mp3Info> list;
    /*音乐播放界面Handler*/
    private Handler uiHandler;
    /*前台通知服务*/
    private NotificationCompat.Builder builder;
    /*Notification的自定义布局*/
    private RemoteViews remoteViews = null;
    /*当前音乐播放界面*/
    private int currentPosition = 0;
    private Mp3Info now_Info = null;
    private Timer timer = null;
    private TimerTask mTimerTask = null;
    private Notification notification;
    private NotificationManager notificationManager = null;
    private SeekBar seekBar = null;
    private boolean flag = false;

    /*音乐播放类型*/
    private enum MusicPlayerType {
        cycle, order, random;
    }

    private enum MusicControl {
        music_continue, music_play;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        init();
        buildNotification();
    }

    private void init() {
        now_Info = (Mp3Info) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_Mp3Info_String);
        list = (List<Mp3Info>) ObjectPool.getInstance().getObject(LocalMusicListActivity.ListMusicInfo_String);//接收到对象池中的ListMusicInfo对象
        seekBar = (SeekBar) ObjectPool.getInstance().getObject(MusicPlayerActivity.SeekBar_String);
        receiver = new ServiceConnectionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ServiceConnection);
        registerReceiver(receiver, filter);//注册接收器
        if (player == null || player.equals(null)) {
            Log.d(TAG, "init: 实例化MediaPlayer");
            player = new MediaPlayer();
        }
        ObjectPool.getInstance().creatObject("MediaPlayer", player);
    }

    private void buildNotification() {
        android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(this);
        remoteViews = new RemoteViews(getPackageName(), R.layout.music_service_notification);
        remoteViews.setTextViewText(R.id.music_notification_musicname, list.get(0).getName());
        Bitmap bitmap = BitmapFactory.decodeFile(list.get(0).getImagePath());
        if (bitmap != null)
            remoteViews.setImageViewBitmap(R.id.music_notification_musicImage, bitmap);
        setNotificationListener();
        builder.setContent(remoteViews);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
        Log.d(TAG, "buildNotification: Notification构建完成");
        music_play(now_Info.getUrl(), MusicControl.music_play);
        updateNotification();
    }

    private void setNotificationListener() {
        Intent intent = new Intent(ACTION_ServiceConnection);
        intent.putExtra(INTENT_BTNID, btn_play);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.music_notification_play_play, pendingIntent);
        Intent intent1 = new Intent(ACTION_ServiceConnection);
        intent1.putExtra(INTENT_BTNID, btn_next);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 2, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.music_notification_play_next, pendingIntent1);
        Intent intent2 = new Intent(ACTION_ServiceConnection);
        intent2.putExtra(INTENT_BTNID, btn_previous);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 3, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.music_notification_play_previous, pendingIntent2);
    }

    private void updateNotification() {
        Log.d(TAG, "updateNotification: 更新Notification");
        remoteViews = notification.contentView;
        now_Info = (Mp3Info) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_Mp3Info_String);
        remoteViews.setTextViewText(R.id.music_notification_musicname, now_Info.getName());
        Bitmap bitmap = BitmapFactory.decodeFile(now_Info.getImagePath());
        if (now_Info.getImagePath() == null) {
            Resources resources = getResources();
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_default_singer);
        }
        if (bitmap != null)
            remoteViews.setImageViewBitmap(R.id.music_notification_musicImage, bitmap);
        player = (MediaPlayer) ObjectPool.getInstance().getObject("MediaPlayer");
        notificationManager.notify(0, notification);
    }
    private void updataNotification_btn(int type) {
        remoteViews = notification.contentView;
        switch (type) {
            case 0:
                Resources resources = getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_music_play);
                remoteViews.setImageViewBitmap(R.id.music_notification_play_play, bitmap);
                break;
            case 1:
                remoteViews.setImageViewResource(R.id.music_notification_play_play, R.drawable.icon_music_pause);
                break;
        }
        notificationManager.notify(0, notification);
    }
    private void playingMusic() {
        if (player != null) {
            seekBar = (SeekBar) ObjectPool.getInstance().getObject(MusicPlayerActivity.SeekBar_String);
            if (timer!=null)
            timer.cancel();
            timer = new Timer();
            seekBar.setMax(player.getDuration());
                        /*定时器刷新进度条*/
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    seekBar.setProgress(player.getCurrentPosition());
                }
            };
            timer.schedule(mTimerTask, 0, 10);
        }
    }

    public void music_play(String uri, final MusicControl control) {
        int index = Utils.getSingInstance().getMusicPostion(uri);
        seekBar = (SeekBar) ObjectPool.getInstance().getObject(MusicPlayerActivity.SeekBar_String);
        Log.d(TAG, "music_play: --------------------------------->");
        if (control == MusicControl.music_play) {
            Log.d(TAG, "music_play: 正在播放...............");
            ObjectPool.getInstance().creatObject(MusicPlayerActivity.NOW_Mp3Info_String, list.get(index));
            updataUI(MusicPlayerActivity.ACTION_FLUSH_PROGRESSBAR);
            updataNotification_btn(1);
            updateNotification();
            try {
                if (timer != null)
                    timer.cancel();
                player.reset();
                player.setDataSource(list.get(index).getUrl());
                player.prepareAsync();//异步装载媒体资源
                /*当装载流媒体完毕的时候回调*/
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        timer = new Timer();
                        seekBar.setMax(player.getDuration());
                        /*定时器刷新进度条*/
                        mTimerTask = new TimerTask() {
                            @Override
                            public void run() {
                                seekBar.setProgress(player.getCurrentPosition());
                            }
                        };
                        timer.schedule(mTimerTask, 0, 10);
                        player.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "music_play: 读取音乐文件流错误");
                Context context = (Context) ObjectPool.getInstance().getObject(MusicPlayerActivity.MusicPlayerContext);
                Toast.makeText(context, "文件读取错误或者不支持，已从列表删除", Toast.LENGTH_SHORT).show();
                music_next();
                removeErrorMusic(list.get(index));
            }
        } else {
            try {
                player.prepare();
                Log.d(TAG, "music_play: 继续音乐");
                player.seekTo(currentPosition);
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeErrorMusic(Mp3Info info) {
        list.remove(info);
        Utils.getSingInstance().saveMusicList(list, getApplicationContext());
    }

    private void music_pause() {
        Log.d(TAG, "暂停音乐");
        updataNotification_btn(0);
        updataUI(MusicPlayerActivity.ACTION_BTNUPDATE_PAUSE);
        if (player != null) {
            flag = true;
            currentPosition = player.getCurrentPosition();
            player.stop();
        }
        updateNotification();
    }

    private void music_next() {
        now_Info = (Mp3Info) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_Mp3Info_String);
        int index = Utils.getSingInstance().getMusicPostion(now_Info.getUrl()) + 1;
        list = (List<Mp3Info>) ObjectPool.getInstance().getObject(LocalMusicListActivity.ListMusicInfo_String);
        if (index == list.size())
            index = 0;
        if (list != null) {
            Log.d(TAG, "music_next: " + index);
            flag = false;
            music_play(list.get(index).getUrl(), MusicControl.music_play);
        }
        Log.d(TAG, "music_next: " + index);
    }

    private void music_previous() {
        now_Info = (Mp3Info) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_Mp3Info_String);
        int index = Utils.getSingInstance().getMusicPostion(now_Info.getUrl()) - 1;
        list = (List<Mp3Info>) ObjectPool.getInstance().getObject(LocalMusicListActivity.ListMusicInfo_String);
        if (index == -1)
            index = list.size() - 1;
        Log.d(TAG, "music_next: 即将播放 " + list.get(index).getName());
        if (list != null) {
            flag = false;
            music_play(list.get(index).getUrl(), MusicControl.music_play);
        }
    }

    private void music_continue() {
        updataNotification_btn(1);
        updataUI(MusicPlayerActivity.ACTION_BTUPNDATA_PLAY);
        Log.d(TAG, "继续音乐");
        if (player != null) {
            Log.d(TAG, "music_continue: .........................?");
            flag = true;
            music_play(now_Info.getUrl(), MusicControl.music_continue);
        }
    }

    private void updataUI(String action) {
        Intent intent = new Intent();
        intent.setAction(MusicPlayerActivity.ACTION_UPDATEUI);
        intent.putExtra("action", action);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
            player = null;
        }
        super.onDestroy();

    }

    private class ServiceConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int btn_id = intent.getIntExtra(INTENT_BTNID, 0);
            Log.d(TAG, "onReceive:" + btn_id);
            if (btn_id != 0) {
                switch (btn_id) {
                    case btn_play:
                        Log.d(TAG, "onReceive: notification 点击了暂停");
                        if (player.isPlaying())
                            music_pause();
                        else
                            music_continue();
                        break;
                    case btn_next:
                        music_next();
                        break;
                    case btn_previous:
                        music_previous();
                        break;
                }
            } else {
                String musicControl = intent.getStringExtra(MusicPlayerActivity.RECEIOVERACTION);
                now_Info = (Mp3Info) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_Mp3Info_String);
                Log.d(TAG, musicControl + " ");  //UI操作
                if (musicControl.equals("play")) {
                    Log.d(TAG, "onReceive: play");
                    music_play(now_Info.getUrl(), MusicControl.music_play);
                } else if (musicControl.equals("pause"))
                    music_pause();
                else if (musicControl.equals("next"))
                    music_next();
                else if (musicControl.equals("previous"))
                    music_previous();
                else if (musicControl.equals("continue")) {
                    updataUI(MusicPlayerActivity.ACTION_BTUPNDATA_PLAY);
                    music_continue();
                }
                else if(musicControl.equals("playingMusic"))
                    playingMusic();
            }
        }
    }
}
