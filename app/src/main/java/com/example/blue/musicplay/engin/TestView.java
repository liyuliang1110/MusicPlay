package com.example.blue.musicplay.engin;

import android.content.Context;

import android.view.View;

/**
 * Created by blue on 2016/9/16.
 */

public class TestView extends View {
    public TestView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
