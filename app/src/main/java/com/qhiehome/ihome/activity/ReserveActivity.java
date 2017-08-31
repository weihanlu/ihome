package com.qhiehome.ihome.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.ericliu.asyncexpandablelist.CollectionView;
import com.ericliu.asyncexpandablelist.async.AsyncExpandableListView;
import com.ericliu.asyncexpandablelist.async.AsyncExpandableListViewCallbacks;
import com.ericliu.asyncexpandablelist.async.AsyncHeaderViewHolder;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.ReserveViewPagerAdapter;
import com.qhiehome.ihome.lock.ConnectLockService;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.order.OrderRequest;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingusing.ParkingUsingRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingusing.ParkingUsingResponse;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingRequest;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingResponse;
import com.qhiehome.ihome.network.model.park.reservecancel.ReserveCancelRequest;
import com.qhiehome.ihome.network.model.park.reservecancel.ReserveCancelResponse;
import com.qhiehome.ihome.network.service.inquiry.OrderService;
import com.qhiehome.ihome.network.service.inquiry.ParkingUsingService;
import com.qhiehome.ihome.network.service.park.EnterParkingService;
import com.qhiehome.ihome.network.service.park.ReserveCancelService;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.NaviUtil;
import com.qhiehome.ihome.util.NetworkUtils;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReserveActivity extends BaseActivity implements AsyncExpandableListViewCallbacks<String, Bitmap> {

    @BindView(R.id.lv_reserve_list)
    AsyncExpandableListView mLvReserve;
    @BindView(R.id.srl_reserve_list)
    SwipeRefreshLayout mSrlReserve;

    MaterialDialog mProgressDialog;
    @BindView(R.id.viewstub_reserve_list)
    ViewStub mViewStub;
    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitleToolbar;

    private ConnectLockReceiver mReceiver;

    private Context mContext;
    private List<OrderResponse.DataBean.OrderListBean> mOrderBeanList = new ArrayList<>();
    private CollectionView.Inventory<String, Bitmap> mInventory;
    private TextView mTvCountDown;
    private MyCountDownTimer mCountDownTimer;
    private NaviUtil mNavi;


    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final String DECIMAL_2 = "%.2f";
    private static final long INTERVAL = 1000L;
    private static final long QUARTER = 1000 * 60 * 15L;

    /********BaiduNavi********/
    private BNRoutePlanNode.CoordinateType mCoordinateType;
    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "ihome";
    private final static String authBaseArr[] =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private final static String authComArr[] = {Manifest.permission.READ_PHONE_STATE};
    private final static int authBaseRequestCode = 1;
    private final static int authComRequestCode = 2;
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static List<Activity> activityList = new LinkedList<Activity>();
    public static final String RESET_END_NODE = "resetEndNode";
    private static final String APP_ID = "9901662";


    private static final int PARKING_USING = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtil.setStatusBarGradient(this);
        setContentView(R.layout.activity_reserve);
        ButterKnife.bind(this);
        mContext = this;

        mLvReserve.setCallbacks(this);

        initToolbar();
        initSwiperRefreshLayout();
        orderRequest();

        mNavi = NaviUtil.getInstance();
        mNavi.setmContext(mContext);
        mNavi.setmActivity(this);
        if (!BaiduNaviManager.isNaviInited()) {
            if (mNavi.initDirs()) {
                mNavi.initNavi();
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new ConnectLockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectLockService.BROADCAST_CONNECT);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onStartLoadingGroup(int groupOrdinal) {
        //只有第一项且第一项不是已取消、已超时、已支付订单时展开显示详细内容
        if (groupOrdinal == 0 && mOrderBeanList.get(0).getState() != Constant.ORDER_STATE_CANCEL && mOrderBeanList.get(0).getState() != Constant.ORDER_STATE_PAID && mOrderBeanList.get(0).getState() != Constant.ORDER_STATE_TIMEOUT) {
            new LoadDataTask(groupOrdinal, mLvReserve).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
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
        if (groupOrdinal != 0 || mOrderBeanList.get(0).getState() == Constant.ORDER_STATE_CANCEL || mOrderBeanList.get(0).getState() == Constant.ORDER_STATE_PAID || mOrderBeanList.get(0).getState() == Constant.ORDER_STATE_TIMEOUT) {
            myHeaderViewHolder.getIvExpansionIndicator().setVisibility(View.INVISIBLE);
            myHeaderViewHolder.getmProgressBar().setVisibility(View.INVISIBLE);
            myHeaderViewHolder.setEnableClick(false);
        } else {
            myHeaderViewHolder.setEnableClick(true);
        }
        String feeInfo;
        switch (mOrderBeanList.get(groupOrdinal).getState()) {
            case Constant.ORDER_STATE_TEMP_RESERVED:
                feeInfo = "待支付：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(groupOrdinal).getPayFee()) + "元（担保费）";
                myHeaderViewHolder.getTv_fee().setText(feeInfo);
                myHeaderViewHolder.getIvState().setVisibility(View.INVISIBLE);
                break;
            case Constant.ORDER_STATE_RESERVED:
                feeInfo = "订单仍在进行...";
                myHeaderViewHolder.getTv_fee().setText(feeInfo);
                myHeaderViewHolder.getIvState().setVisibility(View.INVISIBLE);
                break;
            case Constant.ORDER_STATE_PARKED:
                feeInfo = "订单仍在进行...";
                myHeaderViewHolder.getTv_fee().setText(feeInfo);
                myHeaderViewHolder.getIvState().setVisibility(View.INVISIBLE);
                break;
            case Constant.ORDER_STATE_NOT_PAID:
                feeInfo = "待支付：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(groupOrdinal).getPayFee()) + "元（停车费）";
                myHeaderViewHolder.getTv_fee().setText(feeInfo);
                myHeaderViewHolder.getIvState().setVisibility(View.INVISIBLE);
                break;
            case Constant.ORDER_STATE_CANCEL:
                feeInfo = "订单已取消";
                myHeaderViewHolder.getTv_fee().setText(feeInfo);
                myHeaderViewHolder.getIvState().setVisibility(View.VISIBLE);
                myHeaderViewHolder.getIvState().setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_order_cancel));
                break;
            case Constant.ORDER_STATE_PAID:
                feeInfo = "已支付：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(groupOrdinal).getPayFee()) + "元";
                myHeaderViewHolder.getTv_fee().setText(feeInfo);
                myHeaderViewHolder.getIvState().setVisibility(View.VISIBLE);
                myHeaderViewHolder.getIvState().setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_order_finish));
                break;
            case Constant.ORDER_STATE_TIMEOUT:
                feeInfo = "超时已扣除：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(groupOrdinal).getPayFee()) + "元（担保费）";
                myHeaderViewHolder.getTv_fee().setText(feeInfo);
                myHeaderViewHolder.getIvState().setVisibility(View.VISIBLE);
                myHeaderViewHolder.getIvState().setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_timeout));
                break;
            default:
                break;
        }
        myHeaderViewHolder.getTv_parking().setText(headerItem);
        myHeaderViewHolder.getTv_time().setText(START_TIME_FORMAT.format(mOrderBeanList.get(groupOrdinal).getStartTime()) + " - " + END_TIME_FORMAT.format(mOrderBeanList.get(groupOrdinal).getEndTime()));
        myHeaderViewHolder.getTv_orderId().setText("订单号：" + String.valueOf(mOrderBeanList.get(groupOrdinal).getId()));
        if (groupOrdinal % 2 == 0) {
            myHeaderViewHolder.getRelativeLayout().setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }

    }

    @Override
    public void bindCollectionItemView(Context context, RecyclerView.ViewHolder holder, final int groupOrdinal, Bitmap item) {
        if (groupOrdinal == 0) {
            final DetailItemHolder detailItemHolder = (DetailItemHolder) holder;
            String info;
            String info2;
            switch (mOrderBeanList.get(0).getState()) {
                case Constant.ORDER_STATE_TEMP_RESERVED://state--30
                    detailItemHolder.getVpReserve().setVisibility(View.GONE);

                    info = "需支付担保费   " + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(0).getPayFee()) + "元";
                    detailItemHolder.getTvDetailInfo().setText(info);
                    mTvCountDown = detailItemHolder.getTvDetailInfo2();

                    long timeRemaining = mOrderBeanList.get(0).getCreateTime() + QUARTER - System.currentTimeMillis();
                    if (mCountDownTimer == null) {
                        mCountDownTimer = new MyCountDownTimer(timeRemaining, INTERVAL);
                    }
                    mCountDownTimer.start();


                    detailItemHolder.getBtnCancel().setVisibility(View.VISIBLE);
                    detailItemHolder.getBtnCancel().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CancelReserve(groupOrdinal);
                        }
                    });

                    detailItemHolder.getBtnFunction().setText("去支付");
                    detailItemHolder.getBtnFunction().setVisibility(View.VISIBLE);
                    detailItemHolder.getBtnFunction().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Pay(groupOrdinal, Constant.PAY_STATE_GUARANTEE);
                        }
                    });

                    detailItemHolder.getBtnNavi().setVisibility(View.INVISIBLE);

                    break;
                case Constant.ORDER_STATE_RESERVED://state--31
                    detailItemHolder.getVpReserve().setVisibility(View.VISIBLE);
                    /*********临时数据**********/
                    List<View> viewList = new ArrayList<>();
                    View view1 = LayoutInflater.from(mContext).inflate(R.layout.item_reserve_viewpager, null);
                    view1.findViewById(R.id.iv_estate_info).setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_estate_map));
                    viewList.add(view1);
                    View view2 = LayoutInflater.from(mContext).inflate(R.layout.item_reserve_viewpager, null);
                    view2.findViewById(R.id.iv_estate_info).setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_identifier));
                    viewList.add(view2);
                    /*********临时数据**********/
                    ReserveViewPagerAdapter viewPagerAdapter = new ReserveViewPagerAdapter(viewList);
                    try {
                        detailItemHolder.getVpReserve().setAdapter(viewPagerAdapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewPagerAdapter.notifyDataSetChanged();
                    }

                    // TODO: 2017/8/28 停车时间改为可配置
                    info = "最晚停车时间   ";
                    info += END_TIME_FORMAT.format(mOrderBeanList.get(0).getStartTime() + QUARTER);
                    detailItemHolder.getTvDetailInfo().setText(info);

                    if (mOrderBeanList.get(0).getStartTime() - System.currentTimeMillis() <= 30 * 60 * 1000 && mOrderBeanList.get(0).getStartTime() - System.currentTimeMillis() >= 0) {
                        detailItemHolder.getBtnFunction().setText("查询可否提前使用");
                        detailItemHolder.getBtnFunction().setVisibility(View.VISIBLE);
                        detailItemHolder.getBtnFunction().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                QueryParkingUsing(detailItemHolder.getBtnFunction());
                            }
                        });
                    } else {
                        detailItemHolder.getBtnFunction().setText("开始停车");
                        detailItemHolder.getBtnFunction().setVisibility(View.VISIBLE);
                        detailItemHolder.getBtnFunction().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LockControl(groupOrdinal, true);
                            }
                        });
                    }


                    detailItemHolder.getBtnNavi().setVisibility(View.VISIBLE);
                    detailItemHolder.getBtnNavi().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Navigation(groupOrdinal);
                        }
                    });

                    detailItemHolder.getBtnCancel().setVisibility(View.VISIBLE);
                    detailItemHolder.getBtnCancel().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CancelReserve(groupOrdinal);
                        }
                    });

                    break;
                case Constant.ORDER_STATE_PARKED://state--32
                    detailItemHolder.getVpReserve().setVisibility(View.VISIBLE);
                    /*********临时数据**********/
                    List<View> viewList2 = new ArrayList<>();
                    View view12 = LayoutInflater.from(mContext).inflate(R.layout.item_reserve_viewpager, null);
                    view12.findViewById(R.id.iv_estate_info).setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_estate_map));
                    viewList2.add(view12);
                    View view22 = LayoutInflater.from(mContext).inflate(R.layout.item_reserve_viewpager, null);
                    view22.findViewById(R.id.iv_estate_info).setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_identifier));
                    viewList2.add(view22);
                    /*********临时数据**********/
                    ReserveViewPagerAdapter viewPagerAdapter2 = new ReserveViewPagerAdapter(viewList2);
                    try {
                        detailItemHolder.getVpReserve().setAdapter(viewPagerAdapter2);
                    } catch (Exception e) {
                        e.printStackTrace();
                        viewPagerAdapter2.notifyDataSetChanged();
                    }
                    info = "停车时间   ";
//                    info += START_TIME_FORMAT.format(SharedPreferenceUtil.getLong(mContext, Constant.PARKING_ENTER_TIME, 0));
                    info += START_TIME_FORMAT.format(mOrderBeanList.get(0).getEnterTime());
                    info += "\n";
                    info += "最晚可停至   ";
                    info += START_TIME_FORMAT.format(mOrderBeanList.get(0).getEndTime());
                    detailItemHolder.getTvDetailInfo().setText(info);

                    detailItemHolder.getBtnNavi().setVisibility(View.VISIBLE);
                    detailItemHolder.getBtnNavi().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Navigation(groupOrdinal);
                        }
                    });

                    detailItemHolder.getBtnFunction().setText("确认离开");
                    detailItemHolder.getBtnFunction().setVisibility(View.VISIBLE);
                    detailItemHolder.getBtnFunction().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LockControl(groupOrdinal, false);
                        }
                    });

                    detailItemHolder.btnCancel.setVisibility(View.VISIBLE);
                    detailItemHolder.btnCancel.setText("暂离");
                    detailItemHolder.btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LockControlSelf();
                        }
                    });
                    break;
                case Constant.ORDER_STATE_NOT_PAID://state--33
                    detailItemHolder.getVpReserve().setVisibility(View.GONE);
                    info = "停车时间   " + START_TIME_FORMAT.format(mOrderBeanList.get(0).getEnterTime());
                    info += "\n";
                    info += "离开时间   " + START_TIME_FORMAT.format(mOrderBeanList.get(0).getEndTime());
                    info += "\n";
                    info += "总金额   " + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(0).getPayFee()) + "元";
                    detailItemHolder.getTvDetailInfo().setText(info);

                    detailItemHolder.getBtnCancel().setVisibility(View.INVISIBLE);

                    detailItemHolder.getBtnNavi().setVisibility(View.INVISIBLE);

                    detailItemHolder.getBtnFunction().setText("去支付");
                    detailItemHolder.getBtnFunction().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Pay(groupOrdinal, Constant.PAY_STATE_TOTAL);
                        }
                    });
                    break;
                case Constant.ORDER_STATE_PAID://state--34
                    break;
                case Constant.ORDER_STATE_TIMEOUT://state--38
                    break;
                case Constant.ORDER_STATE_CANCEL://state--39
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
            if (listviewRef.get() != null) {
                listviewRef.get().onFinishLoadingGroup(mGroupOrdinal, items);
            }
        }

    }

    /*********列表展开holoder**********/
    public static class DetailItemHolder extends RecyclerView.ViewHolder {

        private final ViewPager vpReserve;
        private final TextView tvDetailInfo;
        private final TextView tvDetailInfo2;
        private final Button btnNavi;
        private final Button btnCancel;
        private final Button btnFunction;

        private final TextView tvTitle;
        private final TextView tvDescription;

        public DetailItemHolder(View v) {
            super(v);
            vpReserve = (ViewPager) v.findViewById(R.id.vp_item_reserve);
            tvDetailInfo = (TextView) v.findViewById(R.id.tv_item_reserve_info);
            tvDetailInfo2 = (TextView) v.findViewById(R.id.tv_item_reserve_info2);
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

        public TextView getTvDetailInfo2() {
            return tvDetailInfo2;
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


    /*********列表holoder**********/
    public static class MyHeaderViewHolder extends AsyncHeaderViewHolder implements AsyncExpandableListView.OnGroupStateChangeListener {

        private final TextView tv_parking;
        private final TextView tv_time;
        private final TextView tv_orderId;
        private final TextView tv_fee;
        private final ProgressBar mProgressBar;
        private ImageView ivExpansionIndicator;
        private RelativeLayout relativeLayout;
        private final ImageView ivState;
        private boolean enableClick;

        public MyHeaderViewHolder(View v, int groupOrdinal, AsyncExpandableListView asyncExpandableListView) {
            super(v, groupOrdinal, asyncExpandableListView);
            tv_parking = (TextView) v.findViewById(R.id.tv_item_reserve_parking);
            tv_time = (TextView) v.findViewById(R.id.tv_item_reserve_time);
            tv_orderId = (TextView) v.findViewById(R.id.tv_item_reserve_orderid);
            tv_fee = (TextView) v.findViewById(R.id.tv_item_reserve_fee);
            mProgressBar = (ProgressBar) v.findViewById(R.id.pb_item_reserve);
            mProgressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF,
                    PorterDuff.Mode.MULTIPLY);
            ivExpansionIndicator = (ImageView) v.findViewById(R.id.iv_item_reserve);
            relativeLayout = (RelativeLayout) v.findViewById(R.id.layout_item_reserve_header);
            ivState = (ImageView) v.findViewById(R.id.iv_item_reserve_cancel);
        }

        public TextView getTv_parking() {
            return tv_parking;
        }

        public TextView getTv_time() {
            return tv_time;
        }

        public TextView getTv_orderId() {
            return tv_orderId;
        }

        public TextView getTv_fee() {
            return tv_fee;
        }

        public RelativeLayout getRelativeLayout() {
            return relativeLayout;
        }

        public ProgressBar getmProgressBar() {
            return mProgressBar;
        }

        public ImageView getIvExpansionIndicator() {
            return ivExpansionIndicator;
        }

        public ImageView getIvState() {
            return ivState;
        }

        public void setEnableClick(boolean enableClick) {
            this.enableClick = enableClick;
        }

        @Override
        public void onGroupStartExpending() {
            if (enableClick) {
                mProgressBar.setVisibility(View.VISIBLE);
                ivExpansionIndicator.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onGroupExpanded() {
            if (enableClick) {
                mProgressBar.setVisibility(View.GONE);
                ivExpansionIndicator.setVisibility(View.VISIBLE);
                //ivExpansionIndicator.setImageResource(R.drawable.ic_arrow_up);
            }
        }

        @Override
        public void onGroupCollapsed() {
            if (enableClick) {
                mProgressBar.setVisibility(View.GONE);
                ivExpansionIndicator.setVisibility(View.VISIBLE);
                //ivExpansionIndicator.setImageResource(R.drawable.ic_arrow_down);
            }
        }
    }


    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long time = millisUntilFinished / 1000;

            if (time <= 59) {
                mTvCountDown.setText(String.format(Locale.CHINA, "剩余支付时间   00:%02d", time));
            } else {
                mTvCountDown.setText(String.format(Locale.CHINA, "剩余支付时间   %02d:%02d", time / 60, time % 60));
            }
        }

        @Override
        public void onFinish() {
            mTvCountDown.setText("剩余支付时间   00:00");
            cancelTimer();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }

    private void cancelTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
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
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
        mTvTitleToolbar.setText("我的预约");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void orderRequest() {
        OrderService orderService = ServiceGenerator.createService(OrderService.class);
        String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, Constant.TEST_PHONE_NUM);
        OrderRequest orderRequest = new OrderRequest(EncryptUtil.encrypt(phoneNum, EncryptUtil.ALGO.SHA_256));
        Call<OrderResponse> call = orderService.order(orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mOrderBeanList = response.body().getData().getOrderList();
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
                mLvReserve.setVisibility(View.INVISIBLE);
                try {
                    View viewStubContent = mViewStub.inflate();     //inflate 方法只能被调用一次
                    Button btnLockControl = (Button) viewStubContent.findViewById(R.id.btn_reserve_nonetwork);
                    switch (SharedPreferenceUtil.getInt(mContext, Constant.ORDER_STATE, 0)) {
                        case Constant.ORDER_STATE_RESERVED:
                            btnLockControl.setText("降车位锁");
                            btnLockControl.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LockControl(0, true);
                                }
                            });
                            break;
                        case Constant.ORDER_STATE_PARKED:
                            btnLockControl.setText("升车位锁");
                            btnLockControl.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LockControl(0, false);
                                }
                            });
                            break;
                        default:
                            btnLockControl.setVisibility(View.INVISIBLE);
                            break;
                    }

                } catch (Exception e) {
                    mViewStub.setVisibility(View.VISIBLE);
                }

            }
        });
    }


    private void updateData() {

        mInventory = new CollectionView.Inventory<>();

        for (int i = 0; i < mOrderBeanList.size(); i++) {
            CollectionView.InventoryGroup<String, Bitmap> group = mInventory.newGroup(i);
            group.setHeaderItem(mOrderBeanList.get(i).getEstate().getName());
        }

        mLvReserve.updateInventory(mInventory);
    }

    /***********按钮功能*************/
    private void CancelReserve(final int index) {        //取消预约
        int orderId = mOrderBeanList.get(index).getId();
        ReserveCancelService reserveCancelService = ServiceGenerator.createService(ReserveCancelService.class);
        Call<ReserveCancelResponse> call = reserveCancelService.reserve(new ReserveCancelRequest(orderId));
        call.enqueue(new Callback<ReserveCancelResponse>() {
            @Override
            public void onResponse(Call<ReserveCancelResponse> call, Response<ReserveCancelResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mOrderBeanList.get(index).setState(Constant.ORDER_STATE_CANCEL);
                    updateData();
                }
            }

            @Override
            public void onFailure(Call<ReserveCancelResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    public void Navigation(final int index) {       //导航
        if (BaiduNaviManager.isNaviInited()) {
            mNavi.setsNodeLocation(new LatLng((double) SharedPreferenceUtil.getFloat(mContext, Constant.CURRENT_LATITUDE, 0), (double) SharedPreferenceUtil.getFloat(mContext, Constant.CURRENT_LONGITUDE, 0)));
            mNavi.setsNodeName("我的位置");
            mNavi.seteNodeLocation(new LatLng(mOrderBeanList.get(index).getEstate().getY(), mOrderBeanList.get(index).getEstate().getX()));
            mNavi.seteNodeName(mOrderBeanList.get(index).getEstate().getName());
            mNavi.routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL);
        }
    }

    private void Pay(final int index, int state) {              //支付
        Intent intent = new Intent(ReserveActivity.this, PayActivity.class);
        intent.putExtra("fee", (float) mOrderBeanList.get(index).getPayFee());
        intent.putExtra("payState", state);
        if (state == Constant.PAY_STATE_GUARANTEE) {
            intent.putExtra("orderId", mOrderBeanList.get(index).getId());
        }
        startActivity(intent);
    }

    private void LockControl(int index, final boolean downLock) {      //控制车位锁
        if (!downLock) {
            Log.e("downLock", "升车位锁");
        }
        final String gateWayId = SharedPreferenceUtil.getString(this, Constant.RESERVE_GATEWAY_ID, "");
        final String lockMac = SharedPreferenceUtil.getString(this, Constant.RESERVE_LOCK_MAC, "");
        final String lockPwd = SharedPreferenceUtil.getString(this, Constant.RESERVE_LOCK_PWD, "");
        if (mProgressDialog == null) {
            mProgressDialog = new MaterialDialog.Builder(mContext)
                    .title("连接中")
                    .content("请等待...")
                    .progress(true, 0)
                    .showListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Intent connectLock = new Intent(mContext, ConnectLockService.class);
                            if (NetworkUtils.isConnected(mContext)) {
                                connectLock.setAction(ConnectLockService.ACTION_GATEWAY_CONNECT);
                                connectLock.putExtra(ConnectLockService.EXTRA_GATEWAY_ID, gateWayId);
                            } else {
                                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                    ToastUtil.showToast(mContext, "不支持蓝牙低能耗特性");
                                    dialog.dismiss();
                                } else {
                                    connectLock.setAction(ConnectLockService.ACTION_BLUETOOTH_CONNECT);
                                    connectLock.putExtra(ConnectLockService.EXTRA_LOCK_PWD, lockPwd);
                                }
                            }
                            connectLock.putExtra(ConnectLockService.EXTRA_LOCK_MAC, lockMac);
                            startService(connectLock);
                        }
                    }).dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (downLock) {
                                Intent downLock = new Intent(mContext, ConnectLockService.class);
                                downLock.setAction(ConnectLockService.ACTION_DOWN_LOCK);
                                startService(downLock);
                            } else {
                                Intent upLock = new Intent(mContext, ConnectLockService.class);
                                upLock.setAction(ConnectLockService.ACTION_UP_LOCK);
                                startService(upLock);
                            }
                        }
                    }).build();
        }
        mProgressDialog.show();

        if (downLock) {  //++降车位锁消息发送成功
            Long currentTime = System.currentTimeMillis();
            //如果有网络尽快发送停车信息，没有网络则暂存本地，有网络时尽快发送
            SharedPreferenceUtil.setLong(mContext, Constant.PARKING_ENTER_TIME, currentTime);
            SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_PARKED);
            if (NetworkUtils.isConnected(mContext)) {
                EnterParkingService enterParkingService = ServiceGenerator.createService(EnterParkingService.class);
                EnterParkingRequest enterParkingRequest = new EnterParkingRequest(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), currentTime);
                Call<EnterParkingResponse> call = enterParkingService.enterParking(enterParkingRequest);
                call.enqueue(new Callback<EnterParkingResponse>() {
                    @Override
                    public void onResponse(Call<EnterParkingResponse> call, Response<EnterParkingResponse> response) {
                        if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                            SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_ENTER_TIME, false);
                        } else {
                            ToastUtil.showToast(mContext, "发送请求失败");
                            SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_ENTER_TIME, true);
                        }
                    }

                    @Override
                    public void onFailure(Call<EnterParkingResponse> call, Throwable t) {
                        ToastUtil.showToast(mContext, "网络连接异常");
                        SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_ENTER_TIME, true);
                    }
                });
            } else {
                SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_ENTER_TIME, true);
            }
            updateData();
        }
        if (!downLock) {  //++升车位锁消息发送成功
            SharedPreferenceUtil.setLong(mContext, Constant.PARKING_LEAVE_TIME, System.currentTimeMillis());
            SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_NOT_PAID);
            updateData();
            // TODO: 2017/8/28 增加停车离开计费接口
        }
    }


    private void LockControlSelf() {
        // TODO: 2017/8/24 增加用户自己控制车位锁界面
    }

    private void QueryParkingUsing(final Button btn) {
        ParkingUsingService parkingUsingService = ServiceGenerator.createService(ParkingUsingService.class);
        ParkingUsingRequest parkingUsingRequest = new ParkingUsingRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.SHA_256));
        Call<ParkingUsingResponse> call = parkingUsingService.parkingUsingQuery(parkingUsingRequest);
        call.enqueue(new Callback<ParkingUsingResponse>() {
            @Override
            public void onResponse(Call<ParkingUsingResponse> call, Response<ParkingUsingResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    btn.setText("降车位锁");
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LockControl(0, true);
                        }
                    });
                } else if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == PARKING_USING) {
                    new MaterialDialog.Builder(mContext)
                            .title("车位占用")
                            .content("车位还不能提前使用")
                            .negativeText("取消")
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ParkingUsingResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    Toast.makeText(mContext, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mNavi.initNavi();
        } else if (requestCode == authComRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                }
            }
            mNavi.routeplanToNavi(mCoordinateType);
        }
    }

    private class ConnectLockReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

}
