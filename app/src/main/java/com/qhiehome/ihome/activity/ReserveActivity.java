package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ericliu.asyncexpandablelist.CollectionView;
import com.ericliu.asyncexpandablelist.async.AsyncExpandableListView;
import com.ericliu.asyncexpandablelist.async.AsyncExpandableListViewCallbacks;
import com.ericliu.asyncexpandablelist.async.AsyncHeaderViewHolder;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.ReserveViewPagerAdapter;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.order.OrderRequest;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.network.service.inquiry.OrderService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.lang.ref.WeakReference;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReserveActivity extends BaseActivity implements AsyncExpandableListViewCallbacks<String, Bitmap> {

    @BindView(R.id.tb_reserve_list)
    Toolbar mTbReserve;
    @BindView(R.id.lv_reserve_list)
    AsyncExpandableListView mLvReserve;
    @BindView(R.id.srl_reserve_list)
    SwipeRefreshLayout mSrlReserve;

    private Context mContext;
    private List<OrderResponse.DataBean.OrderBean> mOrderBeanList = new ArrayList<>();
    private CollectionView.Inventory<String, Bitmap> mInventory;

    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final String DECIMAL_2 = "%.2f";

    private static final int ORDER_STATE_TEMP_RESERVED = 30;//btn：取消+支付  info：剩余支付时间，支付金额
    private static final int ORDER_STATE_RESERVED = 31;//取消+导航+升降车位锁+小区地图+出入证  info：最晚停车时间
    private static final int ORDER_STATE_PARKED = 32;//导航+升降车位锁+小区地图  info：停车时间+最晚离开时间
    private static final int ORDER_STATE_NOT_PAID = 33;//支付 info：支付金额
    private static final int ORDER_STATE_PAID = 34;//NA  info：支付金额
    private static final int ORDER_STATE_TIMEOUT = 38;//支付 info：支付金额
    private static final int ORDER_STATE_CANCEL = 39;//NA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
        ButterKnife.bind(this);
        mContext = this;

        mLvReserve.setCallbacks(this);

        initToolbar();
        initSwiperRefreshLayout();
        orderRequest();

    }

    @Override
    public void onStartLoadingGroup(int groupOrdinal) {
        new LoadDataTask(groupOrdinal, mLvReserve).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public RecyclerView.ViewHolder newCollectionItemView(Context context, int groupOrdinal, ViewGroup parent) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_reserve_detail, parent, false);

        return new DetailItemHolder(v);
    }

    @Override
    public AsyncHeaderViewHolder newCollectionHeaderView(Context context, int groupOrdinal, ViewGroup parent) {
        // Create a new view.
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_reserve_header, parent, false);

        return new MyHeaderViewHolder(v, groupOrdinal, mLvReserve);
    }

    @Override
    public void bindCollectionHeaderView(Context context, AsyncHeaderViewHolder holder, int groupOrdinal, String headerItem) {
        MyHeaderViewHolder myHeaderViewHolder = (MyHeaderViewHolder) holder;
        myHeaderViewHolder.getTv_parking().setText(headerItem);
        myHeaderViewHolder.getTv_time().setText(START_TIME_FORMAT.format(mOrderBeanList.get(groupOrdinal).getStartTime()) + " - " + END_TIME_FORMAT.format(mOrderBeanList.get(groupOrdinal).getEndTime()));
        myHeaderViewHolder.getTv_orderId().setText("订单号：" + String.valueOf(mOrderBeanList.get(groupOrdinal).getId()));
        if (groupOrdinal%2 == 0){
            myHeaderViewHolder.getRelativeLayout().setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }

    }

    @Override
    public void bindCollectionItemView(Context context, RecyclerView.ViewHolder holder, int groupOrdinal, Bitmap item) {
        if (groupOrdinal == 0){
            DetailItemHolder detailItemHolder = (DetailItemHolder) holder;
            String info;
            switch (mOrderBeanList.get(0).getState()){
                case ORDER_STATE_TEMP_RESERVED:
                    detailItemHolder.getVpReserve().setVisibility(View.GONE);
                    info = "需支付担保费：" + String.format(DECIMAL_2, mOrderBeanList.get(0).getPayFee()) + "元";
                    info += "\n";
                    info += "剩余时间：" + "15：00分钟";
                    detailItemHolder.getTvDetailInfo().setText(info);
                    detailItemHolder.getBtnCancel().setVisibility(View.VISIBLE);
                    detailItemHolder.getBtnFunction().setText("去支付");
                    detailItemHolder.getBtnNavi().setVisibility(View.INVISIBLE);
                    break;
                case ORDER_STATE_RESERVED:
                    /*********临时数据**********/
                    List<View> viewList = new ArrayList<>();
                    View view1 =  LayoutInflater.from(mContext).inflate(R.layout.item_reserve_viewpager, null);
                    view1.findViewById(R.id.iv_estate_info).setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_estate_map));
                    viewList.add(view1);
                    View view2 =  LayoutInflater.from(mContext).inflate(R.layout.item_reserve_viewpager, null);
                    view2.findViewById(R.id.iv_estate_info).setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_identifier));
                    viewList.add(view2);
                    /*********临时数据**********/
                    ReserveViewPagerAdapter viewPagerAdapter = new ReserveViewPagerAdapter(viewList);
                    try {
                        detailItemHolder.getVpReserve().setAdapter(viewPagerAdapter);
                    }catch (Exception e){
                        e.printStackTrace();
                        viewPagerAdapter.notifyDataSetChanged();
                    }
                    info = "最晚停车时间：";
                    info += END_TIME_FORMAT.format(mOrderBeanList.get(0).getStartTime() + 15*60*1000);
                    detailItemHolder.getTvDetailInfo().setText(info);
                    detailItemHolder.getBtnFunction().setText("降车位锁");
                    break;
                case ORDER_STATE_PARKED:
                    /*********临时数据**********/
                    List<View> viewList2 = new ArrayList<>();
                    View view12 =  LayoutInflater.from(mContext).inflate(R.layout.item_reserve_viewpager, null);
                    view12.findViewById(R.id.iv_estate_info).setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_estate_map));
                    viewList2.add(view12);
                    View view22 =  LayoutInflater.from(mContext).inflate(R.layout.item_reserve_viewpager, null);
                    view22.findViewById(R.id.iv_estate_info).setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_identifier));
                    viewList2.add(view22);
                    /*********临时数据**********/
                    ReserveViewPagerAdapter viewPagerAdapter2 = new ReserveViewPagerAdapter(viewList2);
                    try {
                        detailItemHolder.getVpReserve().setAdapter(viewPagerAdapter2);
                    }catch (Exception e){
                        e.printStackTrace();
                        viewPagerAdapter2.notifyDataSetChanged();
                    }
                    info = "停车时间：";
                    info += START_TIME_FORMAT.format(SharedPreferenceUtil.getLong(mContext, Constant.PARKING_START_TIME, 0));
                    info += "\n";
                    info += "最晚可停至：";
                    info += START_TIME_FORMAT.format(mOrderBeanList.get(0).getEndTime());
                    detailItemHolder.getTvDetailInfo().setText(info);
                    detailItemHolder.getBtnFunction().setText("升车位锁");
                    detailItemHolder.btnCancel.setVisibility(View.INVISIBLE);
                    break;
                case ORDER_STATE_NOT_PAID:
                    detailItemHolder.getVpReserve().setVisibility(View.GONE);
                    info = "停车时间：" + START_TIME_FORMAT.format(SharedPreferenceUtil.getLong(mContext, Constant.PARKING_START_TIME, 0));
                    info += "\n";
                    info += "离开时间：" + START_TIME_FORMAT.format(SharedPreferenceUtil.getLong(mContext, Constant.PARKING_END_TIME, 0));
                    info += "\n";
                    info += "总金额：" + String.format(DECIMAL_2, mOrderBeanList.get(0).getPayFee()) + "元";
                    detailItemHolder.getTvDetailInfo().setText(info);
                    detailItemHolder.getBtnCancel().setVisibility(View.INVISIBLE);
                    detailItemHolder.getBtnFunction().setText("去支付");
                    break;
                case ORDER_STATE_PAID:
                    break;
                case ORDER_STATE_TIMEOUT:
                    break;
                case ORDER_STATE_CANCEL:
                    break;
                default:
                    break;

            }
        }
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, Void> {

        private final int mGroupOrdinal;
        private WeakReference<AsyncExpandableListView<String, Bitmap>> listviewRef = null;

        public LoadDataTask(int groupOrdinal, AsyncExpandableListView<String, Bitmap> listview) {
            mGroupOrdinal = groupOrdinal;
            listviewRef = new WeakReference<>(listview);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // TODO: 2017/8/18 访问服务器获取小区地图和通行证
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            List<Bitmap> items = new ArrayList<>();
            items.add(null);
            if (listviewRef.get() != null){
                listviewRef.get().onFinishLoadingGroup(mGroupOrdinal, items);
            }
        }

    }

    /*********列表展开holoder**********/
    public static class DetailItemHolder extends RecyclerView.ViewHolder {

        private final ViewPager vpReserve;
        private final TextView tvDetailInfo;
        private final Button btnNavi;
        private final Button btnCancel;
        private final Button btnFunction;

        private final TextView tvTitle;
        private final TextView tvDescription;

        public DetailItemHolder(View v) {
            super(v);
            vpReserve = (ViewPager) v.findViewById(R.id.vp_item_reserve);
            tvDetailInfo = (TextView) v.findViewById(R.id.tv_item_reserve_info);
            btnNavi = (Button) v.findViewById(R.id.btn_item_reserve_navi);
            btnFunction = (Button) v.findViewById(R.id.btn_item_reserve_function);
            btnCancel = (Button) v.findViewById(R.id.btn_item_reserve_cancel);
            tvTitle = (TextView) v.findViewById(R.id.title);
            tvDescription = (TextView) v.findViewById(R.id.description);
        }

        public ViewPager getVpReserve() {
            return vpReserve;
        }

        public TextView getTvDetailInfo() {
            return tvDetailInfo;
        }

        public Button getBtnNavi() {
            return btnNavi;
        }

        public Button getBtnCancel() {
            return btnCancel;
        }

        public Button getBtnFunction() {
            return btnFunction;
        }

        public TextView getTextViewTitle() {
            return tvTitle;
        }

        public TextView getTextViewDescrption() {
            return tvDescription;
        }
    }


    // TODO: 2017/8/18 从第二项开始取消点击事件,增加导航，取消预约按键功能
    /*********列表holoder**********/
    public static class MyHeaderViewHolder extends AsyncHeaderViewHolder implements AsyncExpandableListView.OnGroupStateChangeListener {

        private final TextView tv_parking;
        private final TextView tv_time;
        private final TextView tv_orderId;
        private final ProgressBar mProgressBar;
        private ImageView ivExpansionIndicator;
        private RelativeLayout relativeLayout;

        public MyHeaderViewHolder(View v, int groupOrdinal, AsyncExpandableListView asyncExpandableListView) {
            super(v, groupOrdinal, asyncExpandableListView);
            tv_parking = (TextView) v.findViewById(R.id.tv_item_reserve_parking);
            tv_time = (TextView) v.findViewById(R.id.tv_item_reserve_time);
            tv_orderId = (TextView) v.findViewById(R.id.tv_item_reserve_orderid);
            mProgressBar = (ProgressBar) v.findViewById(R.id.pb_item_reserve);
            mProgressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF,
                    android.graphics.PorterDuff.Mode.MULTIPLY);
            ivExpansionIndicator = (ImageView) v.findViewById(R.id.iv_item_reserve);
            relativeLayout = (RelativeLayout) v.findViewById(R.id.layout_item_reserve_header);
        }

        public TextView getTv_parking() {
            return tv_parking;
        }

        public TextView getTv_time(){
            return tv_time;
        }

        public TextView getTv_orderId(){
            return tv_orderId;
        }

        public RelativeLayout getRelativeLayout(){
            return relativeLayout;
        }

        public ProgressBar getmProgressBar() {
            return mProgressBar;
        }

        public ImageView getIvExpansionIndicator() {
            return ivExpansionIndicator;
        }

        @Override
        public void onGroupStartExpending() {
            mProgressBar.setVisibility(View.VISIBLE);
            ivExpansionIndicator.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onGroupExpanded() {
            mProgressBar.setVisibility(View.GONE);
            ivExpansionIndicator.setVisibility(View.VISIBLE);
            //ivExpansionIndicator.setImageResource(R.drawable.ic_arrow_up);
        }

        @Override
        public void onGroupCollapsed() {
            mProgressBar.setVisibility(View.GONE);
            ivExpansionIndicator.setVisibility(View.VISIBLE);
            //ivExpansionIndicator.setImageResource(R.drawable.ic_arrow_down);

        }
    }



    public void initSwiperRefreshLayout() {
        mSrlReserve = (SwipeRefreshLayout) findViewById(R.id.srl_reserve_list);
        mSrlReserve.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                orderRequest();
            }
        });
        mSrlReserve.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSrlReserve.setRefreshing(true);

    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ReserveActivity.class);
        context.startActivity(intent);
    }


    private void initToolbar() {
        setSupportActionBar(mTbReserve);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbReserve.setTitle("我的预约");
        mTbReserve.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mTbReserve.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void orderRequest(){
        OrderService orderService = ServiceGenerator.createService(OrderService.class);
        String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, Constant.TEST_PHONE_NUM);
        OrderRequest orderRequest = new OrderRequest(EncryptUtil.encrypt(phoneNum, EncryptUtil.ALGO.SHA_256));
        Call<OrderResponse> call = orderService.order(orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE){
                    mOrderBeanList = response.body().getData().getOrder();
//                    if (mReserveAdapter != null) {
//                        mReserveAdapter.notifyDataSetChanged();
//                    }
                    updateData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSrlReserve.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
                mSrlReserve.setRefreshing(false);
            }
        });
    }


    private void updateData(){
        mInventory = new CollectionView.Inventory<>();

        for (int i = 0; i<mOrderBeanList.size(); i++){
            CollectionView.InventoryGroup<String, Bitmap> group = mInventory.newGroup(i);
            group.setHeaderItem(mOrderBeanList.get(i).getEstate().getName());
        }

        mLvReserve.updateInventory(mInventory);
    }


}
