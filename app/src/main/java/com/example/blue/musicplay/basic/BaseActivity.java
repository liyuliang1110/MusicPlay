package com.example.blue.musicplay.basic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by blue on 2016/9/3.
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.lidroid.xutils.ViewUtils.inject(this);
        setView();
        init();
    }

    protected abstract void init();
    protected abstract void setView();

}
