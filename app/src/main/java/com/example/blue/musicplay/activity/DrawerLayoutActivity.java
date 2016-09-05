package com.example.blue.musicplay.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.Service.ControlServic;
import com.example.blue.musicplay.Service.MusicService;
import com.example.blue.musicplay.basic.AcitivtyList;
import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.engin.MusicScan;
import com.example.blue.musicplay.pool.ObjectPool;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by blue on 2016/8/9.
 */
public class DrawerLayoutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "DrawerLayoutActivity --->";
    public static final String THIS_CONTEXT_STRING = "DrawerLaoutActivity_Context";
    @ViewInject(R.id.tuijian_listview)
    private ListView recommend;
    @ViewInject(R.id.open_list)
    private ImageView image_open;
    @ViewInject(R.id.local_music)
    private RelativeLayout local_music;
    @ViewInject(R.id.nav_view)
    private NavigationView navigationView;
    @ViewInject(R.id.drawer_layout)
    private DrawerLayout mDrawerLayout;
    @ViewInject(R.id.toolbar)
    private Toolbar toolbar;
    /*调用Service里面的方法*/
    private ControlServic controlServic;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        com.lidroid.xutils.ViewUtils.inject(this);
        init();
        initMusicService();
    }

    public void init() {
        setSupportActionBar(toolbar);
        init_navigation();
        navigationViewOnclick();
        AcitivtyList.getSingInstance().getList().add(this);
        recommend.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getData()));
        ObjectPool.getInstance().creatObject(THIS_CONTEXT_STRING,this); //存储Context
        loadMusicInfo();
    }
    private void loadMusicInfo() {
        SharedPreferences preferences = getSharedPreferences("data",Context.MODE_PRIVATE);
        String musicList_string = preferences.getString("music_list",null);
        if (musicList_string==null||musicList_string=="") {
            List<Mp3Info> listMusic = MusicScan.getMusicData(getApplicationContext()); //获取音乐列表
            saveMusicList(listMusic);
            Log.d(TAG, "loadMusicInfo: 储存对象成功");
            ObjectPool.getInstance().getObject(LocalMusicListActivity.ListMusicInfo_String);
        }else {
            List<Mp3Info> list = getMusicList(musicList_string);
            if (list!=null) {
                ObjectPool.getInstance().creatObject(LocalMusicListActivity.ListMusicInfo_String, list);
            }else {
                Toast.makeText(this,"读取音乐错误，请确认你的手机中是否有音乐",Toast.LENGTH_SHORT).show();
            }
        }
    }
    /*
        利用Base64编码存储对象
     */
    private void saveMusicList(List<Mp3Info> list) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(list);
            String base64 = new String(Base64.encodeBase64(outputStream.toByteArray()));
            SharedPreferences.Editor editor = getSharedPreferences("data",Context.MODE_PRIVATE).edit();
            editor.putString("music_list",base64);
           editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    * 将获取到的base64编码 读取封装成对象
    * */
    private List<Mp3Info> getMusicList(String base64) {
        List<Mp3Info> list = null;
        byte[] byteBase64 = Base64.decodeBase64(base64.getBytes());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBase64);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            list = (List<Mp3Info>) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init_navigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void navigationViewOnclick() {
        navigationView.setNavigationItemSelectedListener(DrawerLayoutActivity.this);
    }

    @OnClick({R.id.open_list, R.id.local_music})
    private void onclick(View view) {

        switch (view.getId()) {
            case R.id.open_list:
                if ((count & 1) == 0) {
                    image_open.setImageResource(R.drawable.close);
                    recommend.setVisibility(View.INVISIBLE);
                } else {
                    image_open.setImageResource(R.drawable.open);
                    recommend.setVisibility(View.VISIBLE);
                }
                count++;
                break;
            case R.id.local_music:
                Log.d(TAG, "本地音乐");
                startActivity(new Intent(DrawerLayoutActivity.this, LocalMusicListActivity.class));
                break;
        }
    }

    private List<String> getData() {
        List<String> data = new ArrayList<String>();
        data.add("测试数据1");
        data.add("测试数据2");
        data.add("测试数据3");
        data.add("测试数据4");
        data.add("测试数据5");
        data.add("测试数据6");
        data.add("测试数据7");
        return data;
    }

    private void initMusicService() {
        Log.d(TAG, "正在初始化MusicServic");
        MediaPlayer player = (MediaPlayer) ObjectPool.getInstance().getObject("MediaPlayer");
        if (!isServiceWork(this, "com.example.blue.musicplay.Service.MusicService") || player != null) {
            Intent intent = new Intent(DrawerLayoutActivity.this, MusicService.class);
            startService(intent);
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        item.setChecked(true);
        int id = item.getItemId();
        if (id == R.id.my_music) {
            Log.d(TAG, "点击");
        } else if (id == R.id.my_download) {
            Log.d(TAG, "点击1");
        } else if (id == R.id.my_musicPost) {
            Log.d(TAG, "点击2");
        } else if (id == R.id.timer_shutdown) {

        } else if (id == R.id.evening_model) {

        } else if (id == R.id.exit) {
            Log.d(TAG, "点击了退出");
            Toast.makeText(DrawerLayoutActivity.this, "点击了退出", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < AcitivtyList.getSingInstance().getList().size(); i++) {
                if (null != AcitivtyList.getSingInstance().getList().get(i)) {
                    Activity activity = (Activity) AcitivtyList.getSingInstance().getList().get(i);
                    activity.finish();
                }
            }
        }
        return false;
    }

    /*
     @param mContext
    * @param serviceName
    *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
    * @return true代表正在运行，false代表服务没有正在运行
    */
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
}
