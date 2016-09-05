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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

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
public class MusicService extends Service {
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
    private Timer timer = null;
    private String now_musicUri;
    private int song_time = 0;
    private int current = 0;
    private Notification notification;
    private NotificationManager notificationManager = null;

    /*音乐播放类型*/
    private enum MusicPlayerType {
        cycle, order, random;
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
        list = (List<Mp3Info>) ObjectPool.getInstance().getObject(LocalMusicListActivity.ListMusicInfo_String);//接收到对象池中的ListMusicInfo对象
        receiver = new ServiceConnectionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ServiceConnection);
        registerReceiver(receiver, filter);//注册接收器
        player.reset();
        try {
            player.setDataSource(list.get(0).getUrl());//设置文件路径
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectPool.getInstance().creatObject(MusicPlayerActivity.NOW_musicUri_String, list.get(0).getUrl());
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (playerType == MusicPlayerType.cycle) {
                    updataUI(MusicPlayerActivity.ACTION_FLUSH_PROGRESSBAR);
                    Log.d(TAG, "循环");//发送循环广播，通知刷新UI
                    music_play(now_musicUri);
                }
            }
        });
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
        builder.setSmallIcon(R.drawable.my_head);
        notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        notification.flags = Notification.FLAG_NO_CLEAR;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
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

    private void updateNotification( ) {
        Log.d(TAG, "updateNotification: 更新Notification");
        remoteViews = notification.contentView;
        Mp3Info info = (Mp3Info) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_Mp3Info_String);
        remoteViews.setTextViewText(R.id.music_notification_musicname, info.getName());
        Bitmap bitmap = BitmapFactory.decodeFile(info.getImagePath());
        if (bitmap != null)
            remoteViews.setImageViewBitmap(R.id.music_notification_musicImage, bitmap);
        if (player.isPlaying()) {
            Resources resources = getResources();
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_music_pause);
            remoteViews.setImageViewBitmap(R.id.music_notification_play_play, bitmap);
        } else {
            remoteViews.setImageViewResource(R.id.music_notification_play_play, R.drawable.icon_music_play);

        }
        notificationManager.notify(0, notification);
    }

    public void music_play(String uri) {
        /*更新当前播放uri*/
        ObjectPool.getInstance().creatObject(MusicPlayerActivity.NOW_musicUri_String, uri);

        uiHandler = (Handler) ObjectPool.getInstance().getObject("uiHandler");
        Log.d(TAG, "开始播放");
        current = 0;
        ObjectPool.getInstance().creatObject("MediaPlayer", player);
        try {
            player.reset();
            player.setDataSource(uri);//设置文件路径
            player.prepare();//准备
            player.start();
            if (uiHandler != null) {
                song_time = player.getDuration() / 100;
                timer = new Timer();
                timer.schedule(new SetprogressBarThread(), 1000, song_time);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
           /*更新Notification*/
        updateNotification();
    }

    private void music_pause() {
        Log.d(TAG, "暂停音乐");
        if (player != null) {
            player.stop();
        }
        updataUI(MusicPlayerActivity.ACTION_BTNUPDATE_PAUSE);
        updateNotification();
    }

    private void music_next() {
        int index = Utils.getSingInstance().getMusicPostion(now_musicUri) + 1;
        list = (List<Mp3Info>) ObjectPool.getInstance().getObject(LocalMusicListActivity.ListMusicInfo_String);
        if (index == list.size())
            index = 1;
        Log.d(TAG, "music_next: 即将播放 " + index);
        if (list != null) {
            ObjectPool.getInstance().creatObject(MusicPlayerActivity.NOW_Mp3Info_String, list.get(index));
            updataUI(MusicPlayerActivity.ACTION_FLUSH_PROGRESSBAR);
            music_play(list.get(index).getUrl());
        }
    }

    private void music_previous() {
        int index = Utils.getSingInstance().getMusicPostion(now_musicUri) - 1;
        list = (List<Mp3Info>) ObjectPool.getInstance().getObject(LocalMusicListActivity.ListMusicInfo_String);
        if (index == -1)
            index = list.size() - 1;
        Log.d(TAG, "music_next: 即将播放 " + list.get(index).getName());
        if (list != null) {
            ObjectPool.getInstance().creatObject(MusicPlayerActivity.NOW_Mp3Info_String, list.get(index));
            updataUI(MusicPlayerActivity.ACTION_FLUSH_PROGRESSBAR);
            music_play(list.get(index).getUrl());
        }
    }

    private void music_continue() {
        Log.d(TAG, "继续音乐");
        try {
            player.prepare();
            player.start();//继续音乐
        } catch (IOException e) {
            e.printStackTrace();
        }
        updataUI(MusicPlayerActivity.ACTION_BTUPNDATA_PLAY);
        updateNotification();
    }

    private void updataUI(String action) {
        Intent intent = new Intent();
        intent.setAction(MusicPlayerActivity.ACTION_UPDATEUI);
        intent.putExtra("action", action);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player = null;
        timer = null;
    }

    class SetprogressBarThread extends TimerTask {
        @Override
        public void run() {
            if (player != null) {
                if (player.isPlaying()) {
                    Message message = new Message();
                    message.what = ++current;
                    if (current > 100) {
                        timer.cancel();
                    }
                    uiHandler.sendMessage(message);
                }
            }
        }
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
                now_musicUri = (String) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_musicUri_String);
                Log.d(TAG, musicControl + " ");  //UI操作
                if (musicControl.equals("play"))
                    music_play(now_musicUri);
                else if (musicControl.equals("pause"))
                    music_pause();
                else if (musicControl.equals("next"))
                    music_next();
                else if (musicControl.equals("previous"))
                    music_previous();
                else if (musicControl.equals("continue"))
                    music_continue();
            }
        }
    }

}
