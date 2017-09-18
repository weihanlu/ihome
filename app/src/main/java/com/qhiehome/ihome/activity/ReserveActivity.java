package com.qhiehome.ihome.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.ReserveListAdapter;
import com.qhiehome.ihome.bean.UserLockBean;
import com.qhiehome.ihome.fragment.EstateMapFragment;
import com.qhiehome.ihome.fragment.EstatePassFragment;
import com.qhiehome.ihome.fragment.UserLockFragment;
import com.qhiehome.ihome.lock.ConnectLockService;
import com.qhiehome.ihome.lock.ble.CommunicationManager;
import com.qhiehome.ihome.lock.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.lock.bluetooth.BluetoothClient;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.order.OrderRequest;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingusing.ParkingUsingRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingusing.ParkingUsingResponse;
import com.qhiehome.ihome.network.model.park.charge.ChargeRequest;
import com.qhiehome.ihome.network.model.park.charge.ChargeResponse;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingRequest;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingResponse;
import com.qhiehome.ihome.network.model.park.reservecancel.ReserveCancelRequest;
import com.qhiehome.ihome.network.model.park.reservecancel.ReserveCancelResponse;
import com.qhiehome.ihome.network.service.inquiry.OrderService;
import com.qhiehome.ihome.network.service.inquiry.ParkingUsingService;
import com.qhiehome.ihome.network.service.park.ChargeService;
import com.qhiehome.ihome.network.service.park.EnterParkingService;
import com.qhiehome.ihome.network.service.park.ReserveCancelService;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.NaviUtil;
import com.qhiehome.ihome.util.NetworkUtils;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.QhAvatarSelectDialog;
import com.qhiehome.ihome.view.QhDeleteItemDialog;
import com.qhiehome.ihome.view.QhLockConnectDialog;
import com.qhiehome.ihome.view.RecyclerViewEmptySupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReserveActivity extends BaseActivity {

    private static final String TAG = "ReserveActivity";

    @BindView(R.id.toolbar_center)
    Toolbar mTbReserve;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitleToolbar;
    @BindView(R.id.viewstub_reserve_list)
    ViewStub mViewStub;
    @BindView(R.id.srl_reserve_list)
    SwipeRefreshLayout mSrlReserve;
    @BindView(R.id.rv_reserve_list)
    RecyclerView mRvReserve;
    @BindView(R.id.tv_item_reserve_parking)
    TextView mTvItemReserveParking;
    @BindView(R.id.tv_item_reserve_state)
    TextView mTvItemReserveState;
    @BindView(R.id.tv_item_reserve_orderid)
    TextView mTvItemReserveOrderid;
    @BindView(R.id.tv_item_reserve_time)
    TextView mTvItemReserveTime;
    @BindView(R.id.tv_item_reserve_fee)
    TextView mTvItemReserveFee;
    @BindView(R.id.ll_reserve_use)
    LinearLayout mLlReserveUse;
    @BindView(R.id.ll_reserve_empty)
    LinearLayout mLlReserveEmpty;

    private Context mContext;
    private NaviUtil mNavi;
    private ConnectLockReceiver mReceiver;
    private List<OrderResponse.DataBean.OrderListBean> mOrderBeanList = new ArrayList<>();
    private ReserveListAdapter mRvAdapter;
    MaterialDialog mProgressDialog;

    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final String DECIMAL_2 = "%.2f";

    /********BaiduNavi********/
    private BNRoutePlanNode.CoordinateType mCoordinateType;
    private final static int authBaseRequestCode = 1;
    private final static int authComRequestCode = 2;

    private static final int REQUEST_ENABLE_BT = 3;

    private static final int PARKING_USING = 201;

    private Button mBtnFunction1;
    private Button mBtnFunction2;

    @BindView(R.id.tl_reserve)
    TabLayout mTlReserve;
    @BindView(R.id.vp_reserve)
    ViewPager mVpReserve;

    QhLockConnectDialog mControlLockDialog;

    private String mGatewayId;
    private String mLockMac;
    private String mLockPwd;
    private String mLockName;

    private boolean mDownLock;

    private int mLockState;

    private boolean isPasswordAlreadySet;

    private boolean isTempParking;

    private ArrayList<String> mTitles;
    private ArrayList<Fragment> mFragments;
    private TabLayoutAdapter mTabAdapter;

    EstateMapFragment mEstateMapFragment;
    EstatePassFragment mEstatePassFragment;

    private boolean mJumpToPay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
        ButterKnife.bind(this);

        mContext = this;
        initToolbar();
        initTabLayout();
        initSwiperRefreshLayout();
        initRecyclerView();

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
    protected void onStart() {
        super.onStart();
        mReceiver = new ConnectLockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectLockService.BROADCAST_CONNECT);
        intentFilter.addAction(BLECommandIntent.RX_CURRENT_STATUS);
        intentFilter.addAction(BLECommandIntent.RX_PASSWORD_RESULT);
        intentFilter.addAction(CommunicationManager.ACTION_CONNECTION_STATE_CHANGE);
        intentFilter.addAction(BLECommandIntent.RX_LOCK_RESULT);
        // rf
        intentFilter.addAction(BLECommandIntent.RX_LOCK_RF_START_UP);
        intentFilter.addAction(BLECommandIntent.RX_LOCK_RF_STOP_UP);
        intentFilter.addAction(BLECommandIntent.RX_LOCK_RF_LOCK_STATE);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        orderRequest();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRvAdapter.cancelTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnect();
        unregisterReceiver(mReceiver);

    }

    private void initToolbar() {
        setSupportActionBar(mTbReserve);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbReserve.setTitle("");
        mTvTitleToolbar.setText("我的预约");
        mTbReserve.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.start(mContext);
            }
        });
    }

    private void initSwiperRefreshLayout() {
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

    private void initRecyclerView() {
        mRvReserve.setLayoutManager(new LinearLayoutManager(this));
        mRvAdapter = new ReserveListAdapter(mOrderBeanList, mContext);
        mRvAdapter.setOnItemClickListener(new ReserveListAdapter.OnClickListener() {
            @Override
            public void onClick(View view, final int i) {
                if(mOrderBeanList.get(i).getState() == Constant.ORDER_STATE_TEMP_RESERVED){
                    QhAvatarSelectDialog dialog = new QhAvatarSelectDialog(mContext, "支付担保费", "取消预约", 1);
                    dialog.setOnItemClickListener(new QhAvatarSelectDialog.OnItemClickListener() {
                        @Override
                        public void onTakePhoto(View view) {
                            Pay(i, mOrderBeanList.get(i).getState() == Constant.ORDER_STATE_TEMP_RESERVED?Constant.PAY_STATE_GUARANTEE:Constant.PAY_STATE_TOTAL);
                        }

                        @Override
                        public void onGallery(View view) {
                            CancelReserve(i);
                        }
                    });
                    dialog.show();
                }else {
                    Pay(i, mOrderBeanList.get(i).getState() == Constant.ORDER_STATE_TEMP_RESERVED?Constant.PAY_STATE_GUARANTEE:Constant.PAY_STATE_TOTAL);
                }
            }
        });
//        mRvReserve.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
//                //判断是当前layoutManager是否为LinearLayoutManager
//                //只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
//                if (layoutManager instanceof LinearLayoutManager) {
//                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
//                    //获取第一个可见view的位置
//                    int firstItemPosition =linearManager.findFirstVisibleItemPosition();
//                    if (firstItemPosition != 0){
//                        mRvAdapter.cancelTimer();
//                    }
//                }
//
//            }
//        });
        mRvReserve.setAdapter(mRvAdapter);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ReserveActivity.class);
        context.startActivity(intent);
    }

    private void updateData() {
        mRvAdapter.setmOrderBeanList(mOrderBeanList);
        mRvAdapter.notifyDataSetChanged();
    }

    public void refreshActivity(){
        orderRequest();
    }

    private void orderRequest() {
        OrderService orderService = ServiceGenerator.createService(OrderService.class);
        String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, Constant.TEST_PHONE_NUM);
        final OrderRequest orderRequest = new OrderRequest(EncryptUtil.encrypt(phoneNum, EncryptUtil.ALGO.RSA));
        Call<OrderResponse> call = orderService.order(orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mOrderBeanList = response.body().getData().getOrderList();
                    if (mOrderBeanList != null && mOrderBeanList.size() > 0) {
                        mLlReserveEmpty.setVisibility(View.INVISIBLE);
                        int orderState = mOrderBeanList.get(0).getState();
                        if (orderState == Constant.ORDER_STATE_NOT_PAID && mJumpToPay){
                            mJumpToPay = false;
                            Pay(0, Constant.PAY_STATE_TOTAL);
                        }
                        if (orderState == Constant.ORDER_STATE_CANCEL ||
                                orderState == Constant.ORDER_STATE_TIMEOUT ||
                                mOrderBeanList.get(0).getId() != SharedPreferenceUtil.getInt(mContext, Constant.ORDER_ID, 0)){
                            SharedPreferenceUtil.setBoolean(mContext, Constant.ADVANCED_USE, false);
                        }//订单取消、超时、更新后，重置"提前使用"信息

                        if (mOrderBeanList.get(0).getState() == Constant.ORDER_STATE_RESERVED || mOrderBeanList.get(0).getState() == Constant.ORDER_STATE_PARKED) {
                            mEstateMapFragment.setOrderListBean(mOrderBeanList.get(0));
                            mSrlReserve.setVisibility(View.GONE);
                            mLlReserveUse.setVisibility(View.VISIBLE);
                            mTvItemReserveParking.setText(mOrderBeanList.get(0).getEstate().getName());
                            mTvItemReserveOrderid.setText("订单号："+mOrderBeanList.get(0).getId());
                            mTvItemReserveTime.setText(START_TIME_FORMAT.format(mOrderBeanList.get(0).getStartTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(0).getEndTime()));
                            mTvItemReserveFee.setText("请停车结束后后务必点击确认离开");
                            mTvItemReserveState.setBackground(ContextCompat.getDrawable(mContext, R.drawable.blue_rect_reserve));
                            mTvItemReserveState.setText("进行中");
                        }else {
                            mSrlReserve.setVisibility(View.VISIBLE);
                            mLlReserveUse.setVisibility(View.GONE);
                            updateData();
                        }
                    }
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
                mSrlReserve.setVisibility(View.VISIBLE);
                mSrlReserve.setRefreshing(false);
                mLlReserveEmpty.setVisibility(View.INVISIBLE);

                try {
                    View viewStubContent = mViewStub.inflate();     //inflate 方法只能被调用一次
                    mBtnFunction1 = (Button) viewStubContent.findViewById(R.id.btn_nonetwork_function1);
                    mBtnFunction2 = (Button) viewStubContent.findViewById(R.id.btn_nonetwork_function2);
                } catch (Exception e) {
                    mViewStub.setVisibility(View.VISIBLE);
                }
                mLlReserveEmpty.setVisibility(View.INVISIBLE);
                switch (SharedPreferenceUtil.getInt(mContext, Constant.ORDER_STATE, 0)) {
                    case Constant.ORDER_STATE_RESERVED:
                        long startTimeMillis = SharedPreferenceUtil.getLong(mContext, Constant.PARKING_START_TIME, 0);
                        int freeCancellationTime = SharedPreferenceUtil.getInt(mContext, Constant.FREE_CANCELLATION_TIME, 0);
                        long freeCancellationTimeMillis = freeCancellationTime * 60 * 1000;
                        if ((startTimeMillis <= System.currentTimeMillis() && startTimeMillis + freeCancellationTimeMillis >= System.currentTimeMillis()) ||
                                (startTimeMillis + freeCancellationTimeMillis >= System.currentTimeMillis() && SharedPreferenceUtil.getBoolean(mContext, Constant.ADVANCED_USE, false))){
                            mBtnFunction1.setText("开始停车");
                            mBtnFunction1.setOnClickListener(new Button.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDownLock = true;
                                    LockControl();
                                }
                            });
                            mBtnFunction1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.round_long_button_blue));
                        }else {
                            mBtnFunction1.setText(END_TIME_FORMAT.format(startTimeMillis) + "后可停车");
                            mBtnFunction1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.round_long_button_gray));
                        }
                        mBtnFunction1.setVisibility(View.VISIBLE);
                        mBtnFunction2.setVisibility(View.INVISIBLE);
                        break;
                    case Constant.ORDER_STATE_PARKED:
                        mBtnFunction1.setText("结束离开");
                        mBtnFunction1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDownLock = false;
                                LockControl();
                            }
                        });
                        mBtnFunction1.setVisibility(View.VISIBLE);

                        mBtnFunction2.setText("暂时离开");
                        mBtnFunction2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LockControlSelf();
                            }
                        });
                        mBtnFunction1.setVisibility(View.VISIBLE);
                        mBtnFunction2.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mBtnFunction1.setVisibility(View.INVISIBLE);
                        mBtnFunction2.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        });
    }


    private void initTabLayout() {
        mTitles = new ArrayList<String>() {{
            add("小区地图");
            add("出入证");
        }};
        mEstateMapFragment = new EstateMapFragment();
        mEstatePassFragment = new EstatePassFragment();
        mFragments = new ArrayList<Fragment>() {{
            add(mEstateMapFragment);
            add(mEstatePassFragment);
        }};
        mTabAdapter = new TabLayoutAdapter(getSupportFragmentManager(), mTitles, mFragments);
        mVpReserve.setAdapter(mTabAdapter);
        mTlReserve.setupWithViewPager(mVpReserve);
    }

    private class TabLayoutAdapter extends FragmentPagerAdapter {

        private ArrayList<String> titles;
        private ArrayList<Fragment> fragments;

        public TabLayoutAdapter(FragmentManager fm, ArrayList<String> titles, ArrayList<Fragment> fragments) {
            super(fm);
            this.titles = titles;
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


    /***********按钮功能*************/
    /**
     * 取消预约
     *
     * @param index 列表位置，目前仅为0
     */
    public void CancelReserve(final int index) {
        int orderId = mOrderBeanList.get(index).getId();
        ReserveCancelService reserveCancelService = ServiceGenerator.createService(ReserveCancelService.class);
        Call<ReserveCancelResponse> call = reserveCancelService.reserve(new ReserveCancelRequest(orderId));
        call.enqueue(new Callback<ReserveCancelResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReserveCancelResponse> call, @NonNull Response<ReserveCancelResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mOrderBeanList.get(index).setState(Constant.ORDER_STATE_CANCEL);
                    updateData();
                    refreshActivity();
//                    new MaterialDialog.Builder(mContext)
//                            .content("您的预约已取消")
//                            .negativeText("确定")
//                            .show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReserveCancelResponse> call, @NonNull Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    /**
     * 导航
     *
     * @param index 列表位置，目前仅为0
     */
    public void Navigation(final int index) {
        if (BaiduNaviManager.isNaviInited()) {
            mNavi.setsNodeLocation(new LatLng((double) SharedPreferenceUtil.getFloat(mContext, Constant.CURRENT_LATITUDE, 0), (double) SharedPreferenceUtil.getFloat(mContext, Constant.CURRENT_LONGITUDE, 0)));
            mNavi.setsNodeName("我的位置");
            mNavi.seteNodeLocation(new LatLng(mOrderBeanList.get(index).getEstate().getY(), mOrderBeanList.get(index).getEstate().getX()));
            mNavi.seteNodeName(mOrderBeanList.get(index).getEstate().getName());
            mNavi.routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL);
        }
    }

    /**
     * 支付担保费与停车费
     *
     * @param index 列表位置，目前仅为0
     * @param state 支付状态码{@link Constant}
     */
    private void Pay(final int index, int state) {              //支付
        Intent intent = new Intent(ReserveActivity.this, PayActivity.class);
        intent.putExtra("fee", (float) mOrderBeanList.get(index).getPayFee());
        intent.putExtra("payState", state);
        if (state == Constant.PAY_STATE_GUARANTEE || state == Constant.PAY_STATE_TOTAL) {
            intent.putExtra("orderId", mOrderBeanList.get(index).getId());
        }
        startActivity(intent);
    }

    /**
     * 开始停车&确认离开
     *
     * 停车与离开后将订单状态记录在本地
     */
    public void LockControl() {
        isTempParking = false;
        if (!mDownLock) {
            Log.e("downLock", "升车位锁");
        }
        connectToLock();
        if (mDownLock) {  //++降车位锁消息发送成功
            Long currentTime = System.currentTimeMillis();
            //如果有网络尽快发送停车信息，没有网络则暂存本地，有网络时尽快发送
            SharedPreferenceUtil.setLong(mContext, Constant.PARKING_ENTER_TIME, currentTime);
            SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_PARKED);
            if (NetworkUtils.isConnected(mContext)) {
                EnterParkingService enterParkingService = ServiceGenerator.createService(EnterParkingService.class);
                EnterParkingRequest enterParkingRequest = new EnterParkingRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.RSA), currentTime);
                Call<EnterParkingResponse> call = enterParkingService.enterParking(enterParkingRequest);
                call.enqueue(new Callback<EnterParkingResponse>() {
                    @Override
                    public void onResponse(Call<EnterParkingResponse> call, Response<EnterParkingResponse> response) {
                        if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                            SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_ENTER_TIME, false);
                            mEstateMapFragment.refreshFragment();
                        } else {
                            ToastUtil.showToast(mContext, "发送请求失败");
                            SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_ENTER_TIME, true);
                            SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_PARKED);
                        }
                    }

                    @Override
                    public void onFailure(Call<EnterParkingResponse> call, Throwable t) {
                        ToastUtil.showToast(mContext, "网络连接异常");
                        SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_ENTER_TIME, true);
                        SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_PARKED);
                    }
                });
            } else {
                SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_ENTER_TIME, true);
                SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_PARKED);
            }
            updateData();
        }
        if (!mDownLock) {  //++升车位锁消息发送成功
            Long currentTime = System.currentTimeMillis();
            SharedPreferenceUtil.setLong(mContext, Constant.PARKING_LEAVE_TIME, currentTime);
            SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_NOT_PAID);
            if (NetworkUtils.isConnected(mContext)) {
                ChargeService chargeService = ServiceGenerator.createService(ChargeService.class);
                ChargeRequest chargeRequest = new ChargeRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.RSA), currentTime);
                Call<ChargeResponse> call = chargeService.charge(chargeRequest);
                call.enqueue(new Callback<ChargeResponse>() {
                    @Override
                    public void onResponse(Call<ChargeResponse> call, Response<ChargeResponse> response) {
                        if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                            SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_LEAVE_TIME, false);
                            mJumpToPay = true;
                            refreshActivity();
                        } else {
                            ToastUtil.showToast(mContext, "发送请求失败");
                            SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_LEAVE_TIME, true);
                            SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_NOT_PAID);
                        }
                    }

                    @Override
                    public void onFailure(Call<ChargeResponse> call, Throwable t) {
                        ToastUtil.showToast(mContext, "网络连接异常");
                        SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_LEAVE_TIME, true);
                        SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_NOT_PAID);
                    }
                });
            } else {
                SharedPreferenceUtil.setBoolean(mContext, Constant.NEED_POST_LEAVE_TIME, true);
                SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_NOT_PAID);
            }
            updateData();
        }
    }

    private void connectToLock() {
        mGatewayId = SharedPreferenceUtil.getString(this, Constant.RESERVE_GATEWAY_ID, "");
        mLockMac = SharedPreferenceUtil.getString(this, Constant.RESERVE_LOCK_MAC, "");
        mLockPwd = SharedPreferenceUtil.getString(this, Constant.RESERVE_LOCK_PWD, "");
        mLockName = SharedPreferenceUtil.getString(this, Constant.RESERVE_LOCK_NAME, "");
        if (!NetworkUtils.isConnected(mContext)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                ToastUtil.showToast(mContext, "初始化蓝牙失败");
            } else {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    showProgressDialog();
                }
            }
        } else {
            showProgressDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            showProgressDialog();
        }
    }

    private void showProgressDialog() {
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
                                connectLock.putExtra(ConnectLockService.EXTRA_GATEWAY_ID, mGatewayId);
                                connectLock.putExtra(ConnectLockService.EXTRA_LOCK_MAC, mLockMac);
                            } else {
                                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                    ToastUtil.showToast(mContext, "不支持蓝牙低能耗特性");
                                    dialog.dismiss();
                                } else {
                                    connectLock.setAction(ConnectLockService.ACTION_BLUETOOTH_CONNECT);
                                    connectLock.putExtra(ConnectLockService.EXTRA_LOCK_NAME, mLockName);
                                }
                            }
                            startService(connectLock);
                        }
                    }).dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (!isTempParking) {
                                if (mDownLock) {
                                    Intent downLock = new Intent(mContext, ConnectLockService.class);
                                    downLock.setAction(ConnectLockService.ACTION_DOWN_LOCK);
                                    LogUtil.d(TAG,"lockState is " + mLockState);
                                    downLock.putExtra(ConnectLockService.ACTION_LOCK_STATE, mLockState);
                                    startService(downLock);
                                } else {
                                    Intent upLock = new Intent(mContext, ConnectLockService.class);
                                    upLock.setAction(ConnectLockService.ACTION_UP_LOCK);
                                    LogUtil.d(TAG,"lockState is " + mLockState);
                                    upLock.putExtra(ConnectLockService.ACTION_LOCK_STATE, mLockState);
                                    startService(upLock);
                                }
                                disconnect();
                            }
                        }
                    }).build();
        }
        mProgressDialog.show();
    }


    public void LockControlSelf() {
        isTempParking = true;
        connectToLock();
    }

    /**
     * 查询车位是否可以提前使用
     */
    public void QueryParkingUsing() {
        ParkingUsingService parkingUsingService = ServiceGenerator.createService(ParkingUsingService.class);
        ParkingUsingRequest parkingUsingRequest = new ParkingUsingRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.RSA));
        Call<ParkingUsingResponse> call = parkingUsingService.parkingUsingQuery(parkingUsingRequest);
        call.enqueue(new Callback<ParkingUsingResponse>() {
            @Override
            public void onResponse(Call<ParkingUsingResponse> call, Response<ParkingUsingResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mEstateMapFragment.setmAdvancedUse(true);
                    SharedPreferenceUtil.setBoolean(mContext, Constant.ADVANCED_USE, true);
                    mEstateMapFragment.refreshFragment();
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
            switch (intent.getAction()) {
                case BLECommandIntent.RX_CURRENT_STATUS:
                    // EVENT_TYPE_CURRENT_STATUS
                    // passwordState: 1: set ; 0: not set
                    // battery:       1
                    // lockState:    1: low ; 2: mid 3: high
                    LogUtil.d(TAG, "password state is " + intent.getIntExtra(BLECommandIntent.EXTRA_MM_SET_ALREADY, -1));
                    LogUtil.d(TAG, "battery state is " + intent.getIntExtra(BLECommandIntent.EXTRA_BATTERY_LEVEL, -1));
                    LogUtil.d(TAG, "lock state is " + intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_STATE, -1));

                    isPasswordAlreadySet = intent.getIntExtra(BLECommandIntent.EXTRA_MM_SET_ALREADY, -1) == 1;
                    // send password
                    Bundle data = new Bundle();
                    int[] password = new int[6];
                    for (int i = 0; i < 6; i++) {
                        password[i] = Integer.
                                valueOf(mLockPwd.substring(i, i + 1));
                    }
                    data.putIntArray(BLECommandIntent.EXTRA_PASSWORD, password);
                    data.putInt(BLECommandIntent.EXTRA_ROLE,
                            UserLockBean.LOCK_ROLE.OWNER.ordinal());

                    LogUtil.d(TAG, "isPassword already set " + isPasswordAlreadySet);
                    if (isPasswordAlreadySet) {
                        CommunicationManager.getInstance().sendBLEEvent(mContext, BLECommandIntent.CHECKING_PASSWORD, data);
                    } else {
                        CommunicationManager.getInstance().sendBLEEvent(mContext, BLECommandIntent.SETTING_PASSWORD, data);
                    }

                    int state = intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_STATE, 3);
                    if (state == 1) {
                        mLockState = BluetoothClient.LOCK_STATE.DOWN.ordinal();
                    } else {
                        mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                    }

                    break;
                case ConnectLockService.BROADCAST_CONNECT:
                    String info = intent.getStringExtra("info");
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        if (info != null) {
                            ToastUtil.showToast(mContext, info);
                        } else {
                            // judge whether is temp parking
                            if (isTempParking) {
                                showControlLockDialog();
                            }
                        }
                    }
                    break;
                case BLECommandIntent.RX_PASSWORD_RESULT:
                    LogUtil.d(TAG, "password is " + mLockPwd);
                    int actionId = intent.getIntExtra(BLECommandIntent.EXTRA_PSW_ACTION, -1);
                    int result = intent.getIntExtra(BLECommandIntent.EXTRA_PSW_RESULT, -1);
                    LogUtil.d(TAG, "actionId is " + actionId + ", result is " + result + ", isPassword set " + isPasswordAlreadySet);
                    if (actionId == 0x01) { // setting
                        checkPassword(result);
                    } else if (actionId == 0x02 && isPasswordAlreadySet) { // checking
                        checkPassword(result);
                    }
                    break;
                case CommunicationManager.ACTION_CONNECTION_STATE_CHANGE:
                    int status = intent.getIntExtra(
                            CommunicationManager.EXTRA_CONNECTION_STATE_NEW,
                            CommunicationManager.STATE_INIT);
                    if (status == CommunicationManager.BLE_DISCONNECTTED) {
                        if (mControlLockDialog != null && mControlLockDialog.isShowing()) {
                            mControlLockDialog.dismiss();
                        }
                    }
                    break;
                case BLECommandIntent.RX_LOCK_RESULT:
                    int lockResult = intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_RESULT, 10);
                    if (lockResult == 0x10 || lockResult == 0x20) {
                        if (mLockState == BluetoothClient.LOCK_STATE.UPPING.ordinal()) {
                            mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                        } else {
                            mLockState = BluetoothClient.LOCK_STATE.DOWN.ordinal();
                        }
                    } else if (lockResult == 0x30) {
                        mLockState = BluetoothClient.LOCK_STATE.ERROR.ordinal();
                        if (mLockState == BluetoothClient.LOCK_STATE.DOWNING.ordinal()) {
                            ToastUtil.showToast(mContext, "降锁错误");
                        } else {
                            ToastUtil.showToast(mContext, "升锁错误");
                        }
                    }
                    LogUtil.d(TAG, "RX_LOCK_RESULT is " + lockResult);
                    break;
                case BLECommandIntent.RX_LOCK_RF_START_UP:
                    mLockState = BluetoothClient.LOCK_STATE.UPPING.ordinal();
                    break;
                case BLECommandIntent.RX_LOCK_RF_STOP_UP:
                    boolean success = intent.getBooleanExtra(BLECommandIntent.EXTRA_LOCK_STOP_UP, false);
                    if (success) {
                        mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                    } else {
                        mLockState = BluetoothClient.LOCK_STATE.ERROR.ordinal();
                    }
                    break;
                case BLECommandIntent.RX_LOCK_RF_LOCK_STATE:
                    boolean isDown = intent.getBooleanExtra(BLECommandIntent.EXTRA_LOCK_RF_LOCK_STATE, false);
                    if (isDown) {
                        mLockState = BluetoothClient.LOCK_STATE.DOWN.ordinal();
                    } else {
                        mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                    }
                    break;
            }
        }
    }

    private void checkPassword(int result) {
        if (result == 0x10) {
            // MM 代表密码
            LogUtil.d(TAG, "password is " + mLockPwd);
            Intent normal = new Intent(ConnectLockService.BROADCAST_CONNECT);
            sendBroadcast(normal);
        } else {
            Intent errPassword = new Intent(ConnectLockService.BROADCAST_CONNECT);
            errPassword.putExtra("info", "密码错误");
            sendBroadcast(errPassword);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.start(mContext);
    }

    public void setDownLock(boolean downLock) {
        mDownLock = downLock;
    }

    private void showControlLockDialog() {
        if (mControlLockDialog == null) {
            mControlLockDialog = new QhLockConnectDialog(mContext);
            mControlLockDialog.setOnItemClickListener(new QhLockConnectDialog.OnItemClickListener() {
                @Override
                public void onLockUp(View view) {
                    Intent upLock = new Intent(mContext, ConnectLockService.class);
                    upLock.setAction(ConnectLockService.ACTION_UP_LOCK);
                    LogUtil.d(TAG,"lockState is " + mLockState);
                    upLock.putExtra(ConnectLockService.ACTION_LOCK_STATE, mLockState);
                    startService(upLock);
                }

                @Override
                public void onLockDown(View view) {
                    Intent downLock = new Intent(mContext, ConnectLockService.class);
                    downLock.setAction(ConnectLockService.ACTION_DOWN_LOCK);
                    LogUtil.d(TAG,"lockState is " + mLockState);
                    downLock.putExtra(ConnectLockService.ACTION_LOCK_STATE, mLockState);
                    startService(downLock);
                }
            });
            mControlLockDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    disconnect();
                }
            });
        }
        mControlLockDialog.show();
    }

    private void disconnect() {
        Intent disConnectLock = new Intent(this, ConnectLockService.class);
        disConnectLock.setAction(ConnectLockService.ACTION_DISCONNECT);
        startService(disConnectLock);
    }

}
