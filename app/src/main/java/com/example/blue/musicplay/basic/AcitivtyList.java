package com.example.blue.musicplay.basic;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blue on 2016/8/17.
 */

public class AcitivtyList {
    private static AcitivtyList instance = null;
    public  List<Activity> acitvityList = new ArrayList<>();

    private AcitivtyList() {

    }
    public static AcitivtyList getSingInstance() {
        if (instance == null) {
            synchronized (AcitivtyList.class) {
                instance = new AcitivtyList();
            }
        }
        return  instance;
    }
    public   List  getList() {
        return  acitvityList;
    }

}
