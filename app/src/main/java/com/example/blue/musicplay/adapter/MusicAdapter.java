package com.example.blue.musicplay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.blue.musicplay.basic.Mp3Info;
import com.example.blue.musicplay.R;

import java.util.List;

public class MusicAdapter extends BaseAdapter {
    private List<Mp3Info> listMusic;
    private Context context;

    public MusicAdapter(Context context, List<Mp3Info> listMusic) {
        this.context = context;
        this.listMusic = listMusic;
    }

    public void setListItem(List<Mp3Info> listMusic) {
        this.listMusic = listMusic;
    }

    @Override
    public int getCount() {
        return listMusic.size();
    }

    @Override
    public Object getItem(int arg0) {
        return listMusic.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View converview, ViewGroup parnet) {
        if (converview ==  null) {
            converview = LayoutInflater.from(context).inflate(R.layout.music_item,null);
        }
        Mp3Info info = listMusic.get(position);
        TextView textView_musicName = (TextView) converview.findViewById(R.id.music_name);
        textView_musicName.setText(info.getName());
        TextView text_musicTime = (TextView) converview.findViewById(R.id.music_time);
        text_musicTime.setText(toTime((int)info.getTime()));
        TextView text_singerName = (TextView) converview.findViewById(R.id.singer_name);
        text_singerName.setText(info.getSinger());
        return converview;
    }
    public String toTime(int time)
    {
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

}