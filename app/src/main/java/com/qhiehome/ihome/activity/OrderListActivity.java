package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.OrderListAdapter;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.order.OrderRequest;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.network.model.inquiry.orderowner.OrderOwnerRequest;
import com.qhiehome.ihome.network.model.inquiry.orderowner.OrderOwnerResponse;
import com.qhiehome.ihome.network.service.inquiry.OrderOwnerService;
import com.qhiehome.ihome.network.service.inquiry.OrderService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    @BindView(R.id.rv_order)
    RecyclerView mRvOrder;
    @BindView(R.id.srl_order_list)
    SwipeRefreshLayout mSrlOrderList;
    @BindView(R.id.tb_order)
    Toolbar mTbOrder;

    private OrderListAdapter mAdapter;
    private Context mContext;
    private Handler mHandler;
    private List<OrderOwnerResponse.DataBean.OrderListBean> mData = new ArrayList<>();
    //private List<Map<String, Objects>> mData = new ArrayList<>();
    private static final int REFRESH_COMPLETE = 1;
    private boolean mFirstInquiry = true;



    private static class OrderListHandler extends Handler{
        private final WeakReference<OrderListActivity> mActivity;
        private OrderListHandler(OrderListActivity orderListActivity){
            mActivity = new WeakReference<OrderListActivity>(orderListActivity);
        }
        @Override
        public void handleMessage(android.os.Message msg)
        {
            OrderListActivity orderListActivity = mActivity.get();
            switch (msg.what)
            {
                case REFRESH_COMPLETE:
                    orderListActivity.mAdapter.notifyDataSetChanged();
                    orderListActivity.mSrlOrderList.setRefreshing(false);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        ButterKnife.bind(this);
        mContext = this;
        mHandler = new OrderListHandler(this);
        initToolbar();
        initRecyclerView();
        mSrlOrderList.setOnRefreshListener(this);
        mSrlOrderList.setRefreshing(true);
        initData();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, OrderListActivity.class);
        context.startActivity(intent);
    }


    private void initData() {
        OrderOwnerService orderOwnerService = ServiceGenerator.createService(OrderOwnerService.class);
        //OrderRequest orderRequest = new OrderRequest(EncryptUtil.encrypt("8888", EncryptUtil.ALGO.SHA_256));
        OrderOwnerRequest orderOwnerRequest = new OrderOwnerRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.SHA_256));
        Call<OrderOwnerResponse> call = orderOwnerService.orderOwner(orderOwnerRequest);
        call.enqueue(new Callback<OrderOwnerResponse>() {
            @Override
            public void onResponse(Call<OrderOwnerResponse> call, Response<OrderOwnerResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mData = response.body().getData().getOrderList();
                    if (mFirstInquiry){
                        mAdapter.notifyDataSetChanged();
                        mSrlOrderList.setRefreshing(false);
                        mFirstInquiry = false;
                    }
                }
            }
            @Override
            public void onFailure(Call<OrderOwnerResponse> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(OrderListActivity.this, "网络连接异常");
                        mSrlOrderList.setRefreshing(false);
                    }
                });
            }
        });

    }

    private void initToolbar() {
        setSupportActionBar(mTbOrder);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbOrder.setTitle("历史订单");
        mTbOrder.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mTbOrder.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        mRvOrder.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new OrderListAdapter(mContext, mData);
        mRvOrder.setAdapter(mAdapter);
        Context context = OrderListActivity.this;
        DividerItemDecoration did = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        mRvOrder.addItemDecoration(did);
    }


    public void onRefresh()
    {
        initData();
        mHandler.sendEmptyMessage(REFRESH_COMPLETE);
    }


}
