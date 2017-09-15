package com.qhiehome.ihome.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.OrderOwnerAdapter;
import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.bean.UserLockBean;
import com.qhiehome.ihome.bean.UserLockBeanDao;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.network.model.inquiry.orderowner.OrderOwnerRequest;
import com.qhiehome.ihome.network.model.inquiry.orderowner.OrderOwnerResponse;
import com.qhiehome.ihome.network.service.inquiry.OrderOwnerService;
import com.qhiehome.ihome.persistence.DaoSession;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;

import org.greenrobot.greendao.query.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderOwnerFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "OrderOwnerFragment";

    @BindView(R.id.rv_order_owner)
    RecyclerView mRvOrderOwner;
    @BindView(R.id.srl_order_owner)
    SwipeRefreshLayout mSrlOrderOwner;
    Unbinder unbinder;

    private Context mContext;
    private Handler mHandler;
    private OrderOwnerAdapter mAdapter;
    private UserLockBeanDao mUserLockBeanDao;
    private Query<UserLockBean> mUserLockBeansQuery;
    private List<UserLockBean> mUserLocks;
    private List<OrderOwnerResponse.DataBean.OrderListBean> mOrderBeanList = new ArrayList<>();

    private static final int REFRESH_COMPLETE = 1;
    private static final int EXTRA_INFO = 2;

    private int mPostTimes;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_owner, container, false);
        unbinder = ButterKnife.bind(this, view);
        mHandler = new OrderOwnerHandler(this);

        mSrlOrderOwner.setOnRefreshListener(this);
        mSrlOrderOwner.setRefreshing(true);
        initParkings();
        refreshData();
        initRecyclerView();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private static class OrderOwnerHandler extends Handler {
        private final WeakReference<OrderOwnerFragment> mFragment;
        private OrderOwnerHandler(OrderOwnerFragment orderOwnerFragment){
            mFragment = new WeakReference<OrderOwnerFragment>(orderOwnerFragment);
        }
        @Override
        public void handleMessage(android.os.Message msg)
        {
            OrderOwnerFragment orderOwnerFragment = mFragment.get();
            switch (msg.what)
            {
                case REFRESH_COMPLETE:
                    orderOwnerFragment.mAdapter.notifyDataSetChanged();
                    orderOwnerFragment.mSrlOrderOwner.setRefreshing(false);
                    break;
                case EXTRA_INFO:
                    orderOwnerFragment.initData();
                default:
                    break;
            }
        }
    }

    private void initRecyclerView(){
        mRvOrderOwner.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new OrderOwnerAdapter(mContext, mOrderBeanList);
        mRvOrderOwner.setAdapter(mAdapter);
        DividerItemDecoration did = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        mRvOrderOwner.addItemDecoration(did);
    }

    private void initParkings(){
        DaoSession daoSession = ((IhomeApplication) getActivity().getApplicationContext()).getDaoSession();
        mUserLockBeanDao = daoSession.getUserLockBeanDao();
        mUserLockBeansQuery = mUserLockBeanDao.queryBuilder().orderAsc(UserLockBeanDao.Properties.Id).build();
        mUserLocks = mUserLockBeansQuery.list();
    }

    private void initData(){
        OrderOwnerService orderOwnerService = ServiceGenerator.createService(OrderOwnerService.class);
        OrderOwnerRequest orderOwnerRequest = new OrderOwnerRequest(mUserLocks.get(mPostTimes).getParkingId());
        Call<OrderOwnerResponse> call = orderOwnerService.orderOwner(orderOwnerRequest);
        call.enqueue(new Callback<OrderOwnerResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderOwnerResponse> call, @NonNull Response<OrderOwnerResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    mOrderBeanList.addAll(response.body().getData().getOrderList());
                    mPostTimes ++;
                    if (mPostTimes == mUserLocks.size()){
                        mHandler.sendEmptyMessage(REFRESH_COMPLETE);//所有车位查询完毕，刷新列表信息
                    }else {
                        mHandler.sendEmptyMessage(EXTRA_INFO);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<OrderOwnerResponse> call, @NonNull Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    public void refreshData(){
        if (mUserLocks.size() > 0){
            mPostTimes = 0;
            mOrderBeanList.clear();
            initData();
        }
    }

}
