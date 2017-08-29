package com.qhiehome.ihome.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qhiehome.ihome.R;

public class WeekPickView extends LinearLayout {

    TextView mTvSunday;
    TextView mTvMonday;
    TextView mTvTuesday;
    TextView mTvWednesday;
    TextView mTvThursday;
    TextView mTvFriday;
    TextView mTvSaturday;

    private boolean[] mSelected;

    private Context mContext;

    public WeekPickView(Context context) {
        this(context, null);
    }

    public WeekPickView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekPickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_date_pickview, this, true);
        init();
    }

    private void init() {
        mSelected = new boolean[]{true, true, true, true, true, true, true};
        mTvSunday = (TextView) findViewById(R.id.tv_sunday);
        mTvMonday = (TextView) findViewById(R.id.tv_monday);
        mTvTuesday = (TextView) findViewById(R.id.tv_tuesday);
        mTvWednesday = (TextView) findViewById(R.id.tv_wednesday);
        mTvThursday = (TextView) findViewById(R.id.tv_thursday);
        mTvFriday = (TextView) findViewById(R.id.tv_friday);
        mTvSaturday = (TextView) findViewById(R.id.tv_saturday);
        initListener();
    }

    private void initListener() {
        mTvSunday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvSunday.setTextColor(mSelected[0]? ContextCompat.getColor(mContext, R.color.colorAccent):
                        ContextCompat.getColor(mContext, R.color.white));
                mTvSunday.setBackground(mSelected[0]? ContextCompat.getDrawable(mContext, R.drawable.bg_default):
                        ContextCompat.getDrawable(mContext, R.drawable.bg_coloraccent));
                mSelected[0] = !mSelected[0];
            }
        });
        mTvMonday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvMonday.setTextColor(mSelected[1]? ContextCompat.getColor(mContext, R.color.colorAccent):
                        ContextCompat.getColor(mContext, R.color.white));
                mTvMonday.setBackground(mSelected[1]? ContextCompat.getDrawable(mContext, R.drawable.bg_default):
                        ContextCompat.getDrawable(mContext, R.drawable.bg_coloraccent));
                mSelected[1] = !mSelected[1];
            }
        });
        mTvTuesday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvTuesday.setTextColor(mSelected[2]? ContextCompat.getColor(mContext, R.color.colorAccent):
                        ContextCompat.getColor(mContext, R.color.white));
                mTvTuesday.setBackground(mSelected[2]? ContextCompat.getDrawable(mContext, R.drawable.bg_default):
                        ContextCompat.getDrawable(mContext, R.drawable.bg_coloraccent));
                mSelected[2] = !mSelected[2];
            }
        });
        mTvWednesday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvWednesday.setTextColor(mSelected[3]? ContextCompat.getColor(mContext, R.color.colorAccent):
                        ContextCompat.getColor(mContext, R.color.white));
                mTvWednesday.setBackground(mSelected[3]? ContextCompat.getDrawable(mContext, R.drawable.bg_default):
                        ContextCompat.getDrawable(mContext, R.drawable.bg_coloraccent));
                mSelected[3] = !mSelected[3];
            }
        });
        mTvThursday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvThursday.setTextColor(mSelected[4]? ContextCompat.getColor(mContext, R.color.colorAccent):
                        ContextCompat.getColor(mContext, R.color.white));
                mTvThursday.setBackground(mSelected[4]? ContextCompat.getDrawable(mContext, R.drawable.bg_default):
                        ContextCompat.getDrawable(mContext, R.drawable.bg_coloraccent));
                mSelected[4] = !mSelected[4];
            }
        });
        mTvFriday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvFriday.setTextColor(mSelected[5]? ContextCompat.getColor(mContext, R.color.colorAccent):
                        ContextCompat.getColor(mContext, R.color.white));
                mTvFriday.setBackground(mSelected[5]? ContextCompat.getDrawable(mContext, R.drawable.bg_default):
                        ContextCompat.getDrawable(mContext, R.drawable.bg_coloraccent));
                mSelected[5] = !mSelected[5];
            }
        });
        mTvSaturday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvSaturday.setTextColor(mSelected[6]? ContextCompat.getColor(mContext, R.color.colorAccent):
                        ContextCompat.getColor(mContext, R.color.white));
                mTvSaturday.setBackground(mSelected[6]? ContextCompat.getDrawable(mContext, R.drawable.bg_default):
                        ContextCompat.getDrawable(mContext, R.drawable.bg_coloraccent));
                mSelected[6] = !mSelected[6];
            }
        });
    }

    public boolean isInvalid() {
        for (boolean selected: mSelected) {
            if (selected) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllWeek() {
        for (boolean selected: mSelected) {
            if (!selected) {
                return false;
            }
        }
        return true;
    }

    public String getSelectDayInfo() {
        String[] dayInfo = {"日", "一", "二", "三", "四", "五", "六"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mSelected.length; i++) {
            if (mSelected[i]) {
                sb.append(dayInfo[i]).append(",");
            }
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

}
