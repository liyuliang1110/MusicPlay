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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.Service.ControlServic;
import com.example.blue.musicplay.Service.MusicService;
import com.example.blue.musicplay.adapter.MainMenuAdapter;
import com.example.blue.musicplay.basic.AcitivtyList;
import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.basic.Utils;
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
    @ViewInject(R.id.main_menu_list)
    private ListView main_menu_list ;
//    @ViewInject(R.id.open_list)
//    private ImageView image_open;
//    @ViewInject(R.id.local_music)
//    private RelativeLayout local_music;
    @ViewInject(R.id.nav_view)
    private NavigationView navigationView;
    @ViewInject(R.id.drawer_layout)
    private DrawerLayout mDrawerLayout;
    @ViewInject(R.id.toolbar)
    private Toolbar toolbar;
    /*调用Service里面的方法*/
    private ControlServic controlServic;
    private List<Mp3Info> listMusic = null;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        com.lidroid.xutils.ViewUtils.inject(this);
        init();
    }

    public void init() {
        setSupportActionBar(toolbar);
        init_navigation();
        navigationViewOnclick();
        AcitivtyList.getSingInstance().getList().add(this);
        recommend.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getData()));
        ObjectPool.getInstance().creatObject(THIS_CONTEXT_STRING,this); //存储Context
        MainMenuAdapter adapter = new MainMenuAdapter(this,recommend);
        main_menu_list.setAdapter(adapter);
        main_menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0)
                    startActivity(new Intent(DrawerLayoutActivity.this, LocalMusicListActivity.class));

            }
        });
        loadMusicInfo();
    }
    private void loadMusicInfo() {
        SharedPreferences preferences = getSharedPreferences("data",Context.MODE_PRIVATE);
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
