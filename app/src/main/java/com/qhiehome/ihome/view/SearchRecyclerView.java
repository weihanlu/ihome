package com.qhiehome.ihome.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by YueMa on 2017/8/7.
 */

public class SearchRecyclerView extends RecyclerView {
    public SearchRecyclerView(Context context) {
        super(context);
    }

    public SearchRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //通过复写其onMeasure方法、达到对ScrollView适配的效果

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
