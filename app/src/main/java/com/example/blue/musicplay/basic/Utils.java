package com.example.blue.musicplay.basic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.example.blue.musicplay.activity.LocalMusicListActivity;
import com.example.blue.musicplay.pool.ObjectPool;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    /*
         利用Base64编码存储对象
      */
    public void saveMusicList(List<Mp3Info> list,Context context) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(list);
            String base64 = new String(Base64.encodeBase64(outputStream.toByteArray()));
            SharedPreferences.Editor editor = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
            editor.putString("music_list",base64);
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    * 将获取到的base64编码 读取封装成对象
    * */
    public List<Mp3Info> getMusicList(String base64) {
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
    public Bitmap blurBitmap(Bitmap bitmap,Context context){

        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(context);

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //Set the radius of the blur
        blurScript.setRadius(25.f);

        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
        bitmap.recycle();

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;


    }
}
