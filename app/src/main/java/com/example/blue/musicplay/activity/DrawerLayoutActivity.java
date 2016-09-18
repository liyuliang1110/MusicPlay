package com.example.blue.musicplay.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.adapter.MainMenuAdapter;
import com.example.blue.musicplay.adapter.MainViewPagerAdapter;
import com.example.blue.musicplay.basic.AcitivtyList;
import com.example.blue.musicplay.basic.StatusBarCompat;
import com.example.blue.musicplay.pool.ObjectPool;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by blue on 2016/8/9.
 */
public class DrawerLayoutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "DrawerLayoutActivity --->";
    public static final String THIS_CONTEXT_STRING = "DrawerLaoutActivity_Context";
    @ViewInject(R.id.nav_view)
    private NavigationView navigationView;
    @ViewInject(R.id.drawer_layout)
    private DrawerLayout mDrawerLayout;
    @ViewInject(R.id.toolbar)
    private Toolbar toolbar;
    @ViewInject(R.id.vPager)
    private ViewPager main_view_pager;
    @ViewInject(R.id.menu_local_music)
    private ImageView menu_local_music;
    @ViewInject(R.id.menu_online_music)
    private ImageView menu_online_music;
    private View main_layout, internet_music_layout;
    private List<View> list_viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        com.lidroid.xutils.ViewUtils.inject(this);
        init();
        initViewPager();
        initMenuImage();
    }

    public void init() {

        setSupportActionBar(toolbar);
        StatusBarCompat.compat(this, getResources().getColor(R.color.status_bar_color));
        init_navigation();
        navigationViewOnclick();
        AcitivtyList.getSingInstance().getList().add(this);
    }

    private void initViewPager() {
        LayoutInflater inflater = getLayoutInflater().from(this);
        main_layout = inflater.inflate(R.layout.content_main, null);
        internet_music_layout = inflater.inflate(R.layout.internet_music_layout, null);
        list_viewPager = new ArrayList<>();
        list_viewPager.add(internet_music_layout);
        list_viewPager.add(main_layout);
        MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(list_viewPager, this);
        main_view_pager.setAdapter(pagerAdapter);
        main_view_pager.setCurrentItem(1);
        menu_local_music.setImageResource(R.drawable.main_menu_chenged);
        main_view_pager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void initMenuImage() {
        menu_online_music.setOnClickListener(new MyViewPagerListener(0));
        menu_local_music.setOnClickListener(new MyViewPagerListener(1));
    }

    private class MyViewPagerListener implements View.OnClickListener {
        private int index = 0;

        public MyViewPagerListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View view) {
            main_view_pager.setCurrentItem(index);
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

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        public void onPageScrollStateChanged(int arg0) {


        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {


        }

        public void onPageSelected(int arg0) {
            if (main_view_pager.getCurrentItem() == 1) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_menu_chenged);
                menu_local_music.setImageBitmap(bitmap);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.menu_online_music);
                menu_online_music.setImageBitmap(bitmap);
            } else if (main_view_pager.getCurrentItem() == 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.menu_online_music_changed);
                menu_online_music.setImageBitmap(bitmap);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_menu);
                menu_local_music.setImageBitmap(bitmap);
            }

        }

    }
}
