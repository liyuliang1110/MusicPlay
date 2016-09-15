package com.example.blue.musicplay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.blue.musicplay.R;


/**
 * Created by blue on 2016/9/15.
 */

public class MainMenuAdapter extends BaseAdapter {
    private final String[] main_Menu_Text = new String[]{
            "  本地音乐",
            "  最近播放",
            "  我的收藏",
            "  我的朋友",
            "  猜你喜欢"
    };
    private final int[] main_menu_icon = new int[]{
            R.drawable.localmusic,
            R.drawable.recentlyplay,
            R.drawable.collection,
            R.drawable.friend,
            R.drawable.like
    };
    private ListView recommend;
    private Context context;
    private int count = 0;

    public MainMenuAdapter(Context context, ListView recommend) {
        this.context = context;
        this.recommend = recommend;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.main_menu_list, null);
            if (i == 4) {
                ImageView list_opne = (ImageView) view.findViewById(R.id.open_list);
                list_opne.setImageResource(R.drawable.open);
                final ImageView open_list = (ImageView) view.findViewById(R.id.open_list);
                open_list.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ((count & 1) == 0) {
                            Animation animation = AnimationUtils.loadAnimation(context, R.anim.open);
                            animation.setFillAfter(true);
                            open_list.startAnimation(animation);
                            recommend.setVisibility(View.INVISIBLE);
                        } else {
                            Animation animation = AnimationUtils.loadAnimation(context, R.anim.close);
                            animation.setFillAfter(true);
                            open_list.startAnimation(animation);
                            recommend.setVisibility(View.VISIBLE);
                        }
                        count++;
                    }
                });
            }
        }
        TextView menu_Text = (TextView) view.findViewById(R.id.menu_text);
        menu_Text.setText(main_Menu_Text[i]);
        ImageView image_icon = (ImageView) view.findViewById(R.id.menu_image);
        image_icon.setImageResource(main_menu_icon[i]);
        return view;
    }
}
