package com.example.blue.musicplay.basic;

import com.example.blue.musicplay.activity.LocalMusicListActivity;
import com.example.blue.musicplay.pool.ObjectPool;

import java.util.List;

/**
 * Created by blue on 2016/8/30.
 */

public class Utils {
    private static Utils instance = null;
    private static List<Mp3Info> music_list;
    public static Utils getSingInstance() {
        if (instance == null) {
            synchronized (AcitivtyList.class) {
                instance = new Utils();
            }
        }
        return  instance;
    }
    public int getMusicPostion(String music_uri) {
        music_list = (List<Mp3Info>) ObjectPool.getInstance().getObject(LocalMusicListActivity.ListMusicInfo_String);
        if (music_list!=null) {
            for (int i = 0;i<music_list.size();i++) {
                if (music_list.get(i).getUrl().equals(music_uri))
                    return i;
            }
        }
        return  0;
    }
}
