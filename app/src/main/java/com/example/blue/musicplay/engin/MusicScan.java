package com.example.blue.musicplay.engin;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.blue.musicplay.activity.MusicPlayerActivity;
import com.example.blue.musicplay.basic.Mp3Info;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blue on 2016/8/12.
 */
public class MusicScan {
    private static Context context = null;
    private static final String TAG = "MusicScan--->";
    public static  List<Mp3Info> getMusicData(Context context) {
        MusicScan.context = context;
        List<Mp3Info> musicList = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        if(cr != null) {
            Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor == null)
                return null;
            if (cursor.moveToFirst()) {
                do {
                    Mp3Info info = new Mp3Info();
                    String title = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String singer = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    if (singer.equals("<unknown>")) {
                        singer ="未知歌手";
                    }
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String imagePath = getMusicImagePath(id);
                    String album = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    long size = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Audio.Media.SIZE));
                    long time = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String url = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.DATA));
                    String name = cursor
                            .getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String sbr = name.substring(name.length() - 3,
                            name.length());
                    if(sbr.equals("mp3"))
                    {
                        info.setTitle(title);
                        info.setSinger(singer);
                        info.setAlbum(album);
                        info.setSize(size);
                        info.setTime(time);
                        info.setUrl(url);
                        info.setName(name);
                        info.setImagePath(imagePath);
                        Log.d(TAG, "getMusicData: "+imagePath);
                        musicList.add(info);
                    }

                }while(cursor.moveToNext());
            }
        }
        return musicList;
    }
    private static String getMusicImagePath(int  music_id) {
        String mUriTrack = "content://media/external/audio/media/#";
        String[] projection = new String[]{"album_id"};
        String selection = "_id = ?";
        String[] selectionArgs = new String[]{Integer.toString(music_id)};
        Cursor cur = context.getContentResolver().query(Uri.parse(mUriTrack), projection, selection, selectionArgs, null);
        int album_id = 0;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_id = cur.getInt(0);
        }
        cur.close();
        cur = null;

        if (album_id < 0) {
            return null;
        }
        String mUriAlbums = "content://media/external/audio/albums";
        projection = new String[]{"album_art"};
        cur = context.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);

        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;

        return album_art;
    }

}
