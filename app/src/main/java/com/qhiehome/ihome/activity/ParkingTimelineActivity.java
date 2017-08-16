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
import android.widget.Button;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.indexable_recyclerview.AlphabetItem;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration;
import com.vivian.timelineitemdecoration.itemdecoration.SpanIndexListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;

public class ParkingTimelineActivity extends AppCompatActivity {

//    @BindView(R.id.rv_parking_timeline)
//    RecyclerView mRvParkingTimeline;
    @BindView(R.id.rv_parking_timeline)
    IndexFastScrollRecyclerView mRvParkingTimeline;
    @BindView(R.id.tb_parking_timeline)
    Toolbar mTbParkingTimeline;
    @BindView(R.id.tv_parking_name)
    TextView mTvParkingName;
    @BindView(R.id.tv_parking_timeline_guaranteeFee_num)
    TextView mTvGuaranteeFee;
    @BindView(R.id.btn_parking_timeline_reserve)
    Button mBtnReserve;

    private Context mContext;
    private ParkingAdapter mAdapter;
    private DotItemDecoration mItemDecoration;
    private ArrayList<Integer> mSectionPositions;
    private List<AlphabetItem> mAlphabetItems;
    private ParkingResponse.DataBean.EstateBean mEstateBean;
    private List<ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean> mShareBeanList = new ArrayList<>();
    private List<Boolean> mSelectedList = new ArrayList<>();
    private int mSelectedNum = 0;
    private float mGruaranteeFee = 0;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH");
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
        mGruaranteeFee = mEstateBean.getGuaranteeFee();
        initToolbar();
        initData();
        initRecyclerView();
        mTvGuaranteeFee.setText("0.00");

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
//                Intent intent = new Intent(ParkingTimelineActivity.this, PayActivity.class);
//                intent.putExtra("grauFee", (float) mEstateBean.getGuaranteeFee());
//                startActivity(intent);
                TextView tv_time = (TextView) view.findViewById(R.id.tv_parking_timeline_time);
                TextView tv_info = (TextView) view.findViewById(R.id.tv_parking_timeline_info);
                mSelectedList.set(i, !mSelectedList.get(i));
                if (mSelectedList.get(i)) {
                    //view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                    mSelectedNum++;
                    tv_time.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    tv_info.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                } else {
                    mSelectedNum--;
                    tv_time.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    tv_info.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                }
                mTvGuaranteeFee.setText(String.format(DECIMAL_2, mSelectedNum * mGruaranteeFee));
            }
        });
        mRvParkingTimeline.setAdapter(mAdapter);
        //set index ui
        mRvParkingTimeline.setIndexTextSize(12);
        mRvParkingTimeline.setIndexBarColor("#33334c");
        mRvParkingTimeline.setIndexBarCornerRadius(0);
        mRvParkingTimeline.setIndexBarTransparentValue((float) 0.4);
        mRvParkingTimeline.setIndexbarMargin(0);
        mRvParkingTimeline.setIndexbarWidth(40);
        mRvParkingTimeline.setPreviewPadding(0);
        mRvParkingTimeline.setIndexBarTextColor("#FFFFFF");
        mRvParkingTimeline.setIndexBarVisibility(true);
        mRvParkingTimeline.setIndexbarHighLateTextColor("#33334c");
        mRvParkingTimeline.setIndexBarHighLateTextVisibility(true);
//        mRvParkingTimeline.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    mRvParkingTimeline.setIndexBarVisibility(true);
//                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    mRvParkingTimeline.setIndexBarVisibility(false);
//                }
//            }
//        });
    }

    private void initData() {
        for (int i = 0; i < mEstateBean.getParking().size(); i++) {
            if (mEstateBean.getParking().get(i).getShare().size() != 0) {
                for (int j = 0; j < mEstateBean.getParking().get(i).getShare().size(); j++) {
                    mShareBeanList.add(mEstateBean.getParking().get(i).getShare().get(j));
                    mSelectedList.add(false);
                }
            }
        }
        Collections.sort(mShareBeanList);

        //Alphabet fast scroller data
        mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i<mShareBeanList.size(); i++){
            Date date = TimeUtil.getInstance().millis2Date(mShareBeanList.get(i).getStartTime());
            String hour = HOUR_FORMAT.format(date);
            if (!strAlphabets.contains(hour)){
                strAlphabets.add(hour);
                mAlphabetItems.add(new AlphabetItem(i, hour, false));
            }
        }
    }

    @OnClick(R.id.btn_parking_timeline_reserve)
    public void onReserveClicked() {
        if (mSelectedNum == 0){
            ToastUtil.showToast(mContext, "请选择车位");
        }else {
            //// TODO: 2017/8/15 预约请求，post shareId数组
            Intent intent = new Intent(ParkingTimelineActivity.this, PayActivity.class);
            intent.putExtra("grauFee", mGruaranteeFee * mSelectedNum);
            intent.putExtra("isPay", true);
            startActivity(intent);
            ParkingTimelineActivity.this.finish();
        }
    }

    class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> implements SectionIndexer {
        @Override
        public ParkingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ParkingViewHolder parkingViewHolder = new ParkingViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_parking_timeline, parent, false));
            return parkingViewHolder;
        }

        @Override
        public void onBindViewHolder(final ParkingViewHolder holder, int position) {
            if (mShareBeanList.size() == 1 && position == 1){//防崩溃
                holder.tv_time.setText("");
                holder.tv_info.setText("");
                holder.itemView.setVisibility(View.INVISIBLE);
            }else {
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
                holder.tv_time.setText(startTime + " - " + endTime);
                long timePeriod = mShareBeanList.get(position).getEndTime() - mShareBeanList.get(position).getStartTime();
                int minutes_total = (int) timePeriod/1000/60;
                int hours = minutes_total/60;
                int minutes = minutes_total%60;
                String time_length = "";
                if (hours == 0){
                    time_length = minutes + "分";
                }else {
                    if (minutes == 0){
                        time_length = hours + "小时";
                    }else {
                        time_length = hours + "小时" + minutes + "分";
                    }
                }
                holder.tv_info.setText(time_length);
                //holder.tv_time.setTextColor(COLORS[position % COLORS.length]);
            }
        }

        @Override
        public int getItemCount() {
            if (mShareBeanList.size() == 1){
                return 2;
            }else {
                return mShareBeanList.size();
            }
        }

        class ParkingViewHolder extends RecyclerView.ViewHolder {
            TextView tv_time;
            TextView tv_info;
            public ParkingViewHolder(View itemView) {
                super(itemView);
                tv_time = (TextView) itemView.findViewById(R.id.tv_parking_timeline_time);
                tv_info = (TextView) itemView.findViewById(R.id.tv_parking_timeline_info);
            }
        }

        public void setOnItemClickListener(OnClickListener listener) {
            this.onClickListener = listener;
        }

        private OnClickListener onClickListener;

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        @Override
        public Object[] getSections() {
            List<String> sections = new ArrayList<>(24);
            mSectionPositions = new ArrayList<>(24);
            for (int i = 0, size = mShareBeanList.size(); i<size; i++){
                String section = String.format(HOUR_FORMAT.format(mShareBeanList.get(i).getStartTime()));
                if (!sections.contains(section)){
                    sections.add(section);
                    mSectionPositions.add(i);
                }
            }
            return sections.toArray(new String[0]);
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return mSectionPositions.get(sectionIndex);
        }
    }

    public interface OnClickListener {
        void onClick(View view, int i);
    }
}
