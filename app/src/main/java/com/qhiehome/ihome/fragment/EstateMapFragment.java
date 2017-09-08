package com.qhiehome.ihome.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.qhiehome.ihome.R;

import com.qhiehome.ihome.activity.ReserveActivity;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.util.Constant;


import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * Created by YueMa on 2017/9/8.
 */

public class EstateMapFragment extends Fragment {

    @BindView(R.id.iv_estate_map)
    ImageView mIvMap;
    @BindView(R.id.tv_remind_info)
    TextView mTvRemind;
    @BindView(R.id.btn_function_1)
    Button mBtnFunction1;
    @BindView(R.id.btn_function_2)
    Button mBtnFunction2;
    @BindView(R.id.btn_function_3)
    Button mBtnFunction3;

    Unbinder unbinder;
    private ReserveActivity mActivity;

    private OrderResponse.DataBean.OrderListBean mOrderBean;

    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);

    private Context mContext;
    private boolean mCanUse = false;


    public EstateMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (ReserveActivity) context;
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_estate_map, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            Bundle bundle = this.getArguments();
            mOrderBean = (OrderResponse.DataBean.OrderListBean) bundle.getSerializable("order");
            refreshUI();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_function_1, R.id.btn_function_2, R.id.btn_function_3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_function_1:
                if (mOrderBean.getState() == Constant.ORDER_STATE_RESERVED){
                    mActivity.CancelReserve(0);
                }
                if (mOrderBean.getState() == Constant.ORDER_STATE_PARKED){
                    mActivity.LockControlSelf();
                }
                break;
            case R.id.btn_function_2:
                if (mOrderBean.getState() == Constant.ORDER_STATE_RESERVED && mCanUse){
                    mActivity.LockControl(0, true);
                    mOrderBean.setState(Constant.ORDER_STATE_PARKED);
                    refreshUI();
                }else if (mOrderBean.getState() == Constant.ORDER_STATE_RESERVED && !mCanUse){
                    mActivity.QueryParkingUsing();
                }
                if (mOrderBean.getState() == Constant.ORDER_STATE_PARKED){
                    mActivity.LockControl(0, false);
                    mActivity.refreshActivity();
                }
                break;
            case R.id.btn_function_3:
                mActivity.Navigation(0);
                break;
        }
    }

    public void setCanUse(boolean canUse){
        mCanUse = canUse;
    }

    public void refreshUI(){
        switch (mOrderBean.getState()){
            case Constant.ORDER_STATE_RESERVED:
                // TODO: 2017/9/8 获取城市参数
                mTvRemind.setText("最晚停车时间："+ END_TIME_FORMAT.format(mOrderBean.getStartTime() + 15*60*1000));
                if ((mOrderBean.getStartTime() - System.currentTimeMillis() <= 30 * 60 * 1000 && mOrderBean.getStartTime() - System.currentTimeMillis() > 0) && !mCanUse) {
                    mBtnFunction2.setText("查询可否使用");
                    mBtnFunction2.setVisibility(View.VISIBLE);
                    mCanUse = false;
                } else if (mOrderBean.getStartTime() - System.currentTimeMillis() <= 0 || mCanUse){
                    mBtnFunction2.setText("开始停车");
                    mBtnFunction2.setVisibility(View.VISIBLE);
                    mCanUse = true;
                } else {
                    mBtnFunction2.setVisibility(View.INVISIBLE);
                    mCanUse = false;
                }
                mBtnFunction1.setText("取消预约");
                mBtnFunction3.setText("地图导航");
                break;
            case Constant.ORDER_STATE_PARKED:
                mTvRemind.setText("最晚离开时间："+ END_TIME_FORMAT.format(mOrderBean.getEndTime()));
                mBtnFunction1.setText("暂时离开");
                mBtnFunction2.setText("离开计费");
                mBtnFunction3.setText("地图导航");
                break;
            default:
                break;
        }
    }

}
