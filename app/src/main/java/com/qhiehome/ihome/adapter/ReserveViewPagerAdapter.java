package com.qhiehome.ihome.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by YueMa on 2017/8/18.
 */

public class ReserveViewPagerAdapter extends PagerAdapter {

    private List<View> mListViews;

    public ReserveViewPagerAdapter(List<View> ListViews) {
        this.mListViews = ListViews;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mListViews.get(position), 0);
        return mListViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return mListViews.size();
    }


}
