package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.order.OrderRequest;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.network.service.inquiry.OrderService;
import com.qhiehome.ihome.util.Constant;
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

    private OrderAdapter mAdapter;
    private Handler mHandler;
    private List<OrderResponse.DataBean.OrderBean> mData = new ArrayList<>();
    //private List<Map<String, Objects>> mData = new ArrayList<>();
    private static final int REFRESH_COMPLETE = 1;
    private boolean mFirstInquiry = true;


    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final DecimalFormat FEE_FORMAT = new DecimalFormat("######0.00");

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

        OrderService orderService = ServiceGenerator.createService(OrderService.class);
        //OrderRequest orderRequest = new OrderRequest(EncryptUtil.encrypt("8888", EncryptUtil.ALGO.SHA_256));
        OrderRequest orderRequest = new OrderRequest("f8cfd23a25811570298c8773bdca4d4d538d0d7fe52f6e5b3aefd08b907c8df2");
        Call<OrderResponse> call = orderService.order(orderRequest);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mData = response.body().getData().getOrder();
                    if (mFirstInquiry){
                        mAdapter.notifyDataSetChanged();
                        mSrlOrderList.setRefreshing(false);
                        mFirstInquiry = false;
                    }
                }
            }
            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
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
        mTbOrder.setTitleTextColor(getResources().getColor(R.color.white));
        mTbOrder.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        mRvOrder.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new OrderAdapter();
        mRvOrder.setAdapter(mAdapter);
        Context context = OrderListActivity.this;
        DividerItemDecoration did = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        mRvOrder.addItemDecoration(did);
    }

    class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(OrderListActivity.this).inflate(R.layout.item_order_list, parent, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
//            holder.tv_estate.setText((String)mData.get(position).get("estate"));
//            holder.tv_time.setText(mData.get(position).get("time_start") + "~" + mData.get(position).get("time_end"));
//            String fee = "";
//            if (((Integer)mData.get(position).get("income_expense")) == 1) {
//                fee += "+";
//                holder.iv_income_expense.setColorFilter(Color.RED);
//            } else {
//                fee += "-";
//                holder.iv_income_expense.setColorFilter(Color.GREEN);
//            }
//            fee += mData.get(position).get("fee");
//            holder.tv_fee.setText(fee);
            OrderResponse.DataBean.OrderBean order = mData.get(position);
            holder.tv_estate.setText(String.valueOf(order.getId()));//订单号
            Date start = TimeUtil.getInstance().millis2Date(order.getEnterTime());
            Date end = TimeUtil.getInstance().millis2Date(order.getLeaveTime());
            holder.tv_time.setText(START_TIME_FORMAT.format(start) + "-" + END_TIME_FORMAT.format(end));
            double pay_fee = order.getPayFee();
            if (pay_fee == 0){
                holder.iv_income_expense.setColorFilter(Color.GREEN);
                holder.tv_fee.setText("");
            }else {
                holder.iv_income_expense.setColorFilter(Color.RED);
                holder.tv_fee.setText(FEE_FORMAT.format(pay_fee));
            }
        }


        @Override
        public int getItemCount() {
            return mData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_estate;
            TextView tv_time;
            TextView tv_fee;
            ImageView iv_income_expense;

            public MyViewHolder(View view) {
                super(view);
                tv_estate = (TextView) view.findViewById(R.id.tv_order_estate);
                tv_time = (TextView) view.findViewById(R.id.tv_order_time);
                tv_fee = (TextView) view.findViewById(R.id.tv_order_fee);
                iv_income_expense = (ImageView) view.findViewById(R.id.iv_order_income_expense);
            }

        }
    }



    public void onRefresh()
    {
        initData();
        mHandler.sendEmptyMessage(REFRESH_COMPLETE);
    }


}
