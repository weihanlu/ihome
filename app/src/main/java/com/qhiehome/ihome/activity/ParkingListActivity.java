package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParkingListActivity extends BaseActivity {

    private static enum ITEM_TYPE{
        ITEM_TYPE_BTN,
        ITEM_TYPE_NO_BTN
    }
    @BindView(R.id.tb_parking)
    Toolbar mTbParking;
    @BindView(R.id.rv_parking)
    RecyclerView mRvParking;
    @BindView(R.id.tv_estate_name)
    TextView mTvEstateName;
    private ParkingListAdapter mAdapter;
    private List<Map<String, String>> parking_data = new ArrayList<>();
    private ParkingResponse.DataBean.EstateBean mEstateBean;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final long QUARTER_TIME = 15*60*1000;
    private float mPrice = 0;
    private String mStartTime;
    private String mEndTime;
    private int mStartHourSelection = 0;
    private int mStartMinSelection = 0;
    private int mEndHourSelection = 0;
    private int mEndMinSelection = 0;
    private ArrayList<ArrayList<String>> mStartMinites = new ArrayList<>();
    private ArrayList<String> mStartHours = new ArrayList<>();
    private ArrayList<String> mMinute = new ArrayList<>();

    private ArrayList<ArrayList<String>> mEndMinites = new ArrayList<>();
    private ArrayList<String> mEndHours = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_list);
        ButterKnife.bind(this);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mEstateBean = (ParkingResponse.DataBean.EstateBean) bundle.getSerializable("estate");
        initToolbar();
        initData();
        initRecyclerView();
    }

    private void initToolbar() {
        setSupportActionBar(mTbParking);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbParking.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTvEstateName.setText(mEstateBean.getName());
    }


    private void initData() {
        mStartTime = TIME_FORMAT.format(TimeUtil.getInstance().millis2Date(System.currentTimeMillis()+QUARTER_TIME));
        mEndTime = TIME_FORMAT.format(TimeUtil.getInstance().millis2Date(System.currentTimeMillis()+QUARTER_TIME*2));
        //初始化开始时间数据源
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeUtil.getInstance().millis2Date(System.currentTimeMillis()+QUARTER_TIME));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        ArrayList<String> minute = new ArrayList<>();

        minute.add(String.format("%02d",min));

        if (min > 0 && min < 15){
            minute.add("15");
            minute.add("30");
            minute.add("45");
        } else if (min >= 15 && min < 30) {
            minute.add("30");
            minute.add("45");
        } else if (min >= 30 && min < 45){
            minute.add("45");
        }else if (min >= 45 && min < 60){
        }
        mStartMinites.add(minute);
        mStartHours.add(String.valueOf(hour));
        hour++;
        mMinute.add("00");
        mMinute.add("15");
        mMinute.add("30");
        mMinute.add("45");
        for (int h = hour; h<24; h++){
            mStartMinites.add(mMinute);
            mStartHours.add(String.valueOf(h));
        }
        //初始化结束时间数据源
        calendar.setTime(TimeUtil.getInstance().millis2Date(System.currentTimeMillis()+QUARTER_TIME*2));
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        ArrayList<String> end_minute = new ArrayList<>();

        end_minute.add(String.format("%02d",min));

        if (min > 0 && min < 15){
            end_minute.add("15");
            end_minute.add("30");
            end_minute.add("45");
        } else if (min >= 15 && min < 30) {
            end_minute.add("30");
            end_minute.add("45");
        } else if (min >= 30 && min < 45){
            end_minute.add("45");
        }else if (min >= 45 && min < 60){
        }
        mEndMinites.add(minute);
        mEndHours.add(String.valueOf(hour));
        hour++;
        for (int h = hour; h<24; h++){
            mEndMinites.add(mMinute);
            mEndHours.add(String.valueOf(h));
        }
        mEndHours.add("24");
        ArrayList<String> lastMinute = new ArrayList<>();
        lastMinute.add("00");
        mEndMinites.add(lastMinute);

    }

    public void onItemClick(View view) {
        int position = mRvParking.getChildAdapterPosition(view);
        if(position == 1){
            OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int options2, int options3, View v) {
                    //返回的分别是三个级别的选中位置
                    mStartHourSelection = options1;
                    mStartMinSelection = options2;
                    mStartTime = mStartHours.get(options1) + ":" + mStartMinites.get(options1).get(options2);

                    mAdapter.notifyDataSetChanged();
                }
            })
                    .setTitleText("开始时间")
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setDividerColor(Color.GREEN)//设置分割线的颜色
                    .setSelectOptions(mStartHourSelection, mStartMinSelection)//默认选中项
                    .setBgColor(Color.BLACK)
                    .setTitleBgColor(Color.DKGRAY)
                    .setTitleColor(Color.LTGRAY)
                    .setCancelColor(Color.YELLOW)
                    .setSubmitColor(Color.YELLOW)
                    .setTextColorCenter(Color.LTGRAY)
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                    //.setLabels("省", "市", "区")
                    .setBackgroundId(0x66000000) //设置外部遮罩颜色
                    .build();
            //pvOptions.setSelectOptions(1,1);
            pvOptions.setPicker(mStartHours, mStartMinites);//二级选择器
            pvOptions.show();
        }
        if (position == 2){
            OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int options2, int options3, View v) {
                    //返回的分别是三个级别的选中位置
                    mEndHourSelection = options1;
                    mEndMinSelection = options2;
                    mEndTime = mStartHours.get(options1) + ":" + mStartMinites.get(options1).get(options2);
                    mAdapter.notifyDataSetChanged();
                }
            })
                    .setTitleText("结束时间")
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setDividerColor(Color.GREEN)//设置分割线的颜色
                    .setSelectOptions(mEndHourSelection, mEndMinSelection)//默认选中项
                    .setBgColor(Color.BLACK)
                    .setTitleBgColor(Color.DKGRAY)
                    .setTitleColor(Color.LTGRAY)
                    .setCancelColor(Color.YELLOW)
                    .setSubmitColor(Color.YELLOW)
                    .setTextColorCenter(Color.LTGRAY)
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                    //.setLabels("省", "市", "区")
                    .setBackgroundId(0x66000000) //设置外部遮罩颜色
                    .build();
            //pvOptions.setSelectOptions(1,1);
            pvOptions.setPicker(mEndHours, mEndMinites);//二级选择器
            pvOptions.show();
        }
    }


    private void initRecyclerView() {
        mRvParking.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ParkingListAdapter();
        mRvParking.setAdapter(mAdapter);
        Context context = ParkingListActivity.this;
        DividerItemDecoration did = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        mRvParking.addItemDecoration(did);
    }


    public class ParkingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_BTN.ordinal()){
            ParkingHolder viewHolder = new ParkingHolder(LayoutInflater.from(ParkingListActivity.this).inflate(R.layout.item_rv_parking_list, parent, false));
            return viewHolder;
        }
        if (viewType == ITEM_TYPE.ITEM_TYPE_NO_BTN.ordinal()){
            ParkingHolderNoBtn viewHolder = new ParkingHolderNoBtn(LayoutInflater.from(ParkingListActivity.this).inflate(R.layout.item_rv_parking_list_nobtn, parent, false));
            return viewHolder;
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 1 || position == 2){
            return ITEM_TYPE.ITEM_TYPE_BTN.ordinal();
        }else {
            return ITEM_TYPE.ITEM_TYPE_NO_BTN.ordinal();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ParkingHolderNoBtn){
            if (position == 0){
                ((ParkingHolderNoBtn)holder).tv_title.setText(mEstateBean.getName());
                ((ParkingHolderNoBtn)holder).tv_content.setText("￥XX/小时");
            }
            if (position == 3) {
                ((ParkingHolderNoBtn)holder).tv_title.setText("停车费");
                ((ParkingHolderNoBtn)holder).tv_content.setText("￥"+String.format("%.2f",mPrice));
            }
        }else if (holder instanceof ParkingHolder){
            if (position == 1){
                ((ParkingHolder)holder).tv_title.setText("开始时间");
                ((ParkingHolder)holder).tv_content.setText(mStartTime);
            }
            if (position == 2){
                ((ParkingHolder)holder).tv_title.setText("结束时间");
                ((ParkingHolder)holder).tv_content.setText(mEndTime);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    class ParkingHolder extends RecyclerView.ViewHolder {
        private TextView tv_title;
        private TextView tv_content;
        private ImageView iv_arrow;
        private ParkingHolder(View view) {
            super(view);
            tv_title = (TextView) view.findViewById(R.id.tv_parking_title);
            tv_content = (TextView) view.findViewById(R.id.tv_parking_content);
            iv_arrow = (ImageView) view.findViewById(R.id.iv_parking_arrow);
        }
    }

    class ParkingHolderNoBtn extends RecyclerView.ViewHolder {
        private TextView tv_title;
        private TextView tv_content;

        private ParkingHolderNoBtn(View view) {
            super(view);
            tv_title = (TextView) view.findViewById(R.id.tv_parking_title_nobtn);
            tv_content = (TextView) view.findViewById(R.id.tv_parking_content_nobtn);
        }
    }
}


}