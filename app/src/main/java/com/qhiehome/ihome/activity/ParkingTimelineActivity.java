package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.util.TimeUtil;
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration;
import com.vivian.timelineitemdecoration.itemdecoration.SpanIndexListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParkingTimelineActivity extends AppCompatActivity {

    @BindView(R.id.rv_parking_timeline)
    RecyclerView mRvParkingTimeline;
    @BindView(R.id.tb_parking_timeline)
    Toolbar mTbParkingTimeline;
    @BindView(R.id.tv_parking_name)
    TextView mTvParkingName;

    private Context mContext;
    private ParkingAdapter mAdapter;
    private DotItemDecoration mItemDecoration;
    private ParkingResponse.DataBean.EstateBean mEstateBean;
    private List<ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean> mShareBeanList = new ArrayList<>();

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final int[] COLORS = {0xffFFAD6C, 0xff62f434, 0xffdeda78, 0xff7EDCFF, 0xff58fdea, 0xfffdc75f};//颜色组
    private static final String DECIMAL_2 = "%.2f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_timeline);
        ButterKnife.bind(this);
        mContext = this;
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mEstateBean = (ParkingResponse.DataBean.EstateBean) bundle.get("estate");
        initToolbar();
        initData();
        initRecyclerView();

    }


    private void initToolbar() {
        setSupportActionBar(mTbParkingTimeline);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTvParkingName.setText(mEstateBean.getName() + "：" + String.format(DECIMAL_2, (float) mEstateBean.getUnitPrice()) + "元/小时");
        mTbParkingTimeline.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        mRvParkingTimeline.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mItemDecoration = new DotItemDecoration
                .Builder(this)
                .setOrientation(DotItemDecoration.VERTICAL)//if you want a horizontal item decoration,remember to set horizontal orientation to your LayoutManager
                .setItemStyle(DotItemDecoration.STYLE_DRAW)
                .setTopDistance(20)//dp
                .setItemInterVal(10)//dp
                .setItemPaddingLeft(20)//default value equals to item interval value
                .setItemPaddingRight(20)//default value equals to item interval value
                .setDotColor(Color.WHITE)
                .setDotRadius(2)//dp
                .setDotPaddingTop(0)
                .setDotInItemOrientationCenter(false)//set true if you want the dot align center
                .setLineColor(Color.BLACK)
                .setLineWidth(4)//dp
                .setEndText("")
                .setTextColor(Color.WHITE)
                .setTextSize(10)//sp
                .setDotPaddingText(2)//dp.The distance between the last dot and the end text
                .setBottomDistance(40)//you can add a distance to make bottom line longer
                .create();
        mItemDecoration.setSpanIndexListener(new SpanIndexListener() {
            @Override
            public void onSpanIndexChange(View view, int spanIndex) {
                view.setBackgroundResource(spanIndex == 0 ? R.drawable.pop_left : R.drawable.pop_right);
            }
        });
        mRvParkingTimeline.addItemDecoration(mItemDecoration);
        mAdapter = new ParkingAdapter();
        mAdapter.setOnItemClickListener(new OnClickListener() {
            @Override
            public void onClick(View view, int i) {
                //预约请求
                Intent intent = new Intent(ParkingTimelineActivity.this, PayActivity.class);
                intent.putExtra("grauFee", (float) mEstateBean.getGuaranteeFee());
                startActivity(intent);

            }
        });
        mRvParkingTimeline.setAdapter(mAdapter);
    }

    private void initData() {
        for (int i = 0; i < mEstateBean.getParking().size(); i++) {
            if (mEstateBean.getParking().get(i).getShare().size() != 0) {
                for (int j = 0; j < mEstateBean.getParking().get(i).getShare().size(); j++) {
                    mShareBeanList.add(mEstateBean.getParking().get(i).getShare().get(j));
                }
            }
        }
        Collections.sort(mShareBeanList);
    }

    class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {
        @Override
        public ParkingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ParkingViewHolder parkingViewHolder = new ParkingViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_parking_timeline, parent, false));
            return parkingViewHolder;
        }

        @Override
        public void onBindViewHolder(final ParkingViewHolder holder, int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                    }
                }
            });
            Date startDate = TimeUtil.getInstance().millis2Date(mShareBeanList.get(position).getStartTime());
            Date endDate = TimeUtil.getInstance().millis2Date(mShareBeanList.get(position).getEndTime());
            String startTime = TIME_FORMAT.format(startDate);
            String endTime = TIME_FORMAT.format(endDate);
            holder.tv_time.setText(startTime + " ~ " + endTime);
            //holder.tv_time.setTextColor(COLORS[position % COLORS.length]);
        }

        @Override
        public int getItemCount() {
            return mShareBeanList.size();
        }

        class ParkingViewHolder extends RecyclerView.ViewHolder {
            TextView tv_time;

            public ParkingViewHolder(View itemView) {
                super(itemView);
                tv_time = (TextView) itemView.findViewById(R.id.tv_parking_timeline_time);
            }
        }

        public void setOnItemClickListener(OnClickListener listener) {
            this.onClickListener = listener;
        }

        private OnClickListener onClickListener;
    }

    public interface OnClickListener {
        void onClick(View view, int i);
    }
}
