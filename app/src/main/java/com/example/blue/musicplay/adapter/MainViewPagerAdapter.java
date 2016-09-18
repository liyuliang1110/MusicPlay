package com.example.blue.musicplay.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.blue.musicplay.R;
import com.example.blue.musicplay.activity.DrawerLayoutActivity;
import com.example.blue.musicplay.activity.LocalMusicListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blue on 2016/9/17.
 */

public class MainViewPagerAdapter extends PagerAdapter {
    private List<View> list = null;
    private Context context ;
    public MainViewPagerAdapter(List<View> list, Context context) {
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }



    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (position == 1) {
            ListView recommend = (ListView) list.get(position).findViewById(R.id.tuijian_listview);
            recommend.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_expandable_list_item_1, getData()));
            MainMenuAdapter adapter = new MainMenuAdapter(context, recommend);
            ListView main_menu_list = (ListView) list.get(position).findViewById(R.id.main_menu_list);
            main_menu_list.setAdapter(adapter);
            main_menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i == 0)
                        context.startActivity(new Intent(context, LocalMusicListActivity.class));
                }
            });
        }
        container.addView(list.get(position),0);
        return list.get(position);
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
}
