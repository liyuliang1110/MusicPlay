package com.example.blue.musicplay.activity;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.adapter.MusicAdapter;
import com.example.blue.musicplay.basic.AcitivtyList;
import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.basic.StatusBarCompat;
import com.example.blue.musicplay.basic.Utils;
import com.example.blue.musicplay.engin.MusicScan;
import com.example.blue.musicplay.pool.ObjectPool;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.List;

public class LocalMusicListActivity extends AppCompatActivity {
    @ViewInject(R.id.music_list)
    private ListView listView;
    @ViewInject(R.id.scan_local_music_pro)
    private ProgressBar scan_local_music_pro ;
    @ViewInject(R.id.music_list_toolbar)
    private Toolbar music_list_toolbar;
    private static final String TAG = "LocalMusicActivity --->";
    public static final String ListMusicInfo_String = "ListMp3Info";
    public static final String Now_Mp3Info = "Now_Mp3Info";
    private static List<Mp3Info> listMusic;
    private static MusicAdapter adapter = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_musci_list);
        setSupportActionBar(music_list_toolbar);
        ViewUtils.inject(this);
        StatusBarCompat.compat(this, getResources().getColor(R.color.status_bar_color));
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
                    Utils.getSingInstance().saveMusicList(listMusic, LocalMusicListActivity.this);
                } else {
                    Intent intent = new Intent(LocalMusicListActivity.this, MusicPlayerActivity.class);
                    Mp3Info info = (Mp3Info) ObjectPool.getInstance().getObject(MusicPlayerActivity.NOW_Mp3Info_String);
                    if (info != null)
                        if (info.getUrl() == listMusic.get(postion).getUrl()) {
                            intent.putExtra("flag", true);
                        }
                    ObjectPool.getInstance().creatObject(MusicPlayerActivity.NOW_Mp3Info_String, listMusic.get(postion));
                    startActivity(intent);
                }
            }
        });
    }

    private void init() {
        AcitivtyList.getSingInstance().getList().add(this);
        Resources resources = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.back);
        BitmapDrawable drawable = new BitmapDrawable(getNewBitmap(bitmap));
        music_list_toolbar.setNavigationIcon(drawable);
        music_list_toolbar.setTitle("本地音乐");
        music_list_toolbar.inflateMenu(R.menu.music_local_list);
        music_list_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalMusicListActivity.this.finish();
            }
        });
        music_list_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.scan_local_music :
                        scan_local_music_pro.setVisibility(View.VISIBLE);
                        scan_local_music();
                        scan_local_music_pro.setVisibility(View.INVISIBLE);
                        break;
                }
                return false;
            }
        });
    }
    private void scan_local_music() {
        List<Mp3Info> list =  MusicScan.getMusicData(getApplicationContext());
        int index = list.size()-listMusic.size();
        Toast.makeText(LocalMusicListActivity.this,"扫描完成，新增了"+index+"首歌曲",Toast.LENGTH_SHORT).show();
        Utils.getSingInstance().saveMusicList(list,this);
        adapter.setListItem(list);
        adapter.notifyDataSetChanged();
    }
    private Bitmap getNewBitmap(Bitmap bitmap ) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int new_width = 100;
        int new_height = 100;
        float scaleWidth = (float) new_width / width;
        float scaleHeight = (float) new_height / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        bitmap = bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LocalMusicListActivity.this.finish();
        }
        return false;
    }

}
