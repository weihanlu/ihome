package com.qhiehome.ihome.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.configuration.city.CityConfigRequest;
import com.qhiehome.ihome.network.model.configuration.city.CityConfigResponse;
import com.qhiehome.ihome.network.model.estate.map.DownloadEstateMapRequest;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.network.service.configuration.CityConfigService;
import com.qhiehome.ihome.network.service.estate.DownloadEstateMapService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.QhDeleteItemDialog;


import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private OrderResponse.DataBean.OrderListBean mOrderListBean;

    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);

    private Context mContext;
    private boolean mCanUse;
    private boolean mAdvancedUse;


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
        mAdvancedUse = SharedPreferenceUtil.getBoolean(mContext, Constant.ADVANCED_USE, false);
        try {
            refreshFragment();
            if (mOrderListBean.getState() == Constant.ORDER_STATE_RESERVED || mOrderListBean.getState() == Constant.ORDER_STATE_PARKED){
                DownloadEstateMap();
            }
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
                if (mOrderListBean.getState() == Constant.ORDER_STATE_RESERVED){//取消预约
                    QhDeleteItemDialog dialog = new QhDeleteItemDialog(mContext, "确认取消此次预约？", 1);
                    dialog.setOnSureCallbackListener(new QhDeleteItemDialog.OnSureCallbackListener() {
                        @Override
                        public void onSure(View view) {
                            mActivity.CancelReserve(0);
                        }
                    });
                    dialog.show();
                }
                if (mOrderListBean.getState() == Constant.ORDER_STATE_PARKED){//暂时离开
                    mActivity.LockControlSelf();
                }
                break;
            case R.id.btn_function_2:
                if (mOrderListBean.getState() == Constant.ORDER_STATE_PARKED){//结束停车
                    String title = "点击『结束停车』后将不能使用车位，确认结束停车？";
                    QhDeleteItemDialog dialog = new QhDeleteItemDialog(mContext, title, 1);
                    dialog.setOnSureCallbackListener(new QhDeleteItemDialog.OnSureCallbackListener() {
                        @Override
                        public void onSure(View view) {
                            mActivity.setDownLock(false);
                            mActivity.LockControl();
                        }
                    });
                    dialog.show();

                }
                if (mOrderListBean.getState() == Constant.ORDER_STATE_RESERVED && mCanUse){//开始停车
                    String title = "点击『开始停车』后开始计费，请您离开后务必点击『结束停车』按钮以确认离开";
                    QhDeleteItemDialog dialog = new QhDeleteItemDialog(mContext, title, 1);
                    dialog.setOnSureCallbackListener(new QhDeleteItemDialog.OnSureCallbackListener() {
                        @Override
                        public void onSure(View view) {
                            mActivity.setDownLock(true);
                            mActivity.LockControl();
                            mOrderListBean.setState(Constant.ORDER_STATE_PARKED);
                        }
                    });
                    dialog.show();
                }else if (mOrderListBean.getState() == Constant.ORDER_STATE_RESERVED && !mCanUse){//查询是否可用
                    mActivity.QueryParkingUsing();
                }
                break;
            case R.id.btn_function_3:
                mActivity.Navigation(0);//导航
                break;
        }
    }

    public void setCanUse(boolean canUse){
        mCanUse = canUse;
    }

    public void setmAdvancedUse(boolean mAdvancedUse) {
        this.mAdvancedUse = mAdvancedUse;
    }

    public void refreshFragment(){
        switch (mOrderListBean.getState()){
            case Constant.ORDER_STATE_RESERVED:
                mTvRemind.setText("正在加载...");
                setRemindInfo();
                //提前半小时可以查询车位是否闲置可用
                if ((mOrderListBean.getStartTime() - System.currentTimeMillis() <= 30 * 60 * 1000 && mOrderListBean.getStartTime() - System.currentTimeMillis() > 0) && !mAdvancedUse) {
                    mBtnFunction2.setText("查询可否使用");
                    mBtnFunction2.setVisibility(View.VISIBLE);
                    mCanUse = false;
                } //已到预约时间或者可以提前使用
                else if (mOrderListBean.getStartTime() - System.currentTimeMillis() <= 0 || mAdvancedUse){
                    mBtnFunction2.setText("开始停车");
                    mBtnFunction2.setVisibility(View.VISIBLE);
                    mCanUse = true;
                } //其他时间或车位不可提前使用
                else {
                    mBtnFunction2.setVisibility(View.INVISIBLE);
                    mCanUse = false;
                }
                mBtnFunction1.setText("取消预约");
                mBtnFunction3.setText("地图导航");
                break;
            case Constant.ORDER_STATE_PARKED:
                mTvRemind.setText("最晚离开时间："+ END_TIME_FORMAT.format(mOrderListBean.getEndTime()));
                mBtnFunction1.setText("暂时离开");
                mBtnFunction2.setText("离开计费");
                mBtnFunction3.setText("地图导航");
                break;
            default:
                break;
        }
    }

    private void DownloadEstateMap(){
        DownloadEstateMapService downloadEstateMapService = ServiceGenerator.createService(DownloadEstateMapService.class);
        DownloadEstateMapRequest downloadEstateMapRequest = new DownloadEstateMapRequest(mOrderListBean.getEstate().getId());
        Call<ResponseBody> call = downloadEstateMapService.downloadMap(downloadEstateMapRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(response.body().bytes(), 0, response.body().bytes().length, new BitmapFactory.Options());
                        mIvMap.setImageBitmap(bitmap);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    //图片加载错误
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //图片加载错误
            }
        });
    }

    private void setRemindInfo(){
        CityConfigService cityConfigService = ServiceGenerator.createService(CityConfigService.class);
        CityConfigRequest cityConfigRequest = new CityConfigRequest(mOrderListBean.getEstate().getId());
        Call<CityConfigResponse> call = cityConfigService.queryCityConfig(cityConfigRequest);
        call.enqueue(new Callback<CityConfigResponse>() {
            @Override
            public void onResponse(Call<CityConfigResponse> call, Response<CityConfigResponse> response) {
                try {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE){
                        long freeCancellationTime = response.body().getData().getFreeCancellationTime();
                        mTvRemind.setText("最晚停车时间："+ END_TIME_FORMAT.format(mOrderListBean.getStartTime() + freeCancellationTime*60*1000));
                    }else {
                        ToastUtil.showToast(mContext, "服务器繁忙，请稍后再试");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtil.showToast(mContext, "服务器错误，请稍后再试");
                }

            }

            @Override
            public void onFailure(Call<CityConfigResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    public void setOrderListBean(OrderResponse.DataBean.OrderListBean orderListBean) {
        this.mOrderListBean = orderListBean;
    }

}
