package com.qhiehome.ihome.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.park.charge.ChargeRequest;
import com.qhiehome.ihome.network.model.park.charge.ChargeResponse;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingRequest;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingResponse;
import com.qhiehome.ihome.network.service.park.ChargeService;
import com.qhiehome.ihome.network.service.park.EnterParkingService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.NetworkUtils;
import com.qhiehome.ihome.util.PersistenceUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by YueMa on 2017/8/22.
 */

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (NetworkUtils.isConnected(context)){
            LogUtil.e("NetworkChanged", "Connected");
            //需要POST停车请求
            if (SharedPreferenceUtil.getBoolean(context, Constant.NEED_POST_ENTER_TIME, false)){
                LogUtil.e("NetworkChanged", "SendEnter");
                EnterParkingService enterParkingService = ServiceGenerator.createService(EnterParkingService.class);
                EnterParkingRequest enterParkingRequest = new EnterParkingRequest(EncryptUtil.rsaEncrypt(PersistenceUtil.getUserInfo(context).getPhoneNum()), SharedPreferenceUtil.getLong(context, Constant.PARKING_ENTER_TIME, 0));
                Call<EnterParkingResponse> call = enterParkingService.enterParking(enterParkingRequest);
                call.enqueue(new Callback<EnterParkingResponse>() {
                    @Override
                    public void onResponse(Call<EnterParkingResponse> call, Response<EnterParkingResponse> response) {
                        if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                            SharedPreferenceUtil.setBoolean(context, Constant.NEED_POST_ENTER_TIME, false);
                        } else {
                            ToastUtil.showToast(context, "发送请求失败");
                            SharedPreferenceUtil.setBoolean(context, Constant.NEED_POST_ENTER_TIME, true);
                        }
                    }

                    @Override
                    public void onFailure(Call<EnterParkingResponse> call, Throwable t) {
                        ToastUtil.showToast(context, "网络连接异常");
                        SharedPreferenceUtil.setBoolean(context, Constant.NEED_POST_ENTER_TIME, true);
                    }
                });
            }
            if (SharedPreferenceUtil.getBoolean(context, Constant.NEED_POST_LEAVE_TIME, false)){
                ChargeService chargeService = ServiceGenerator.createService(ChargeService.class);
                ChargeRequest chargeRequest = new ChargeRequest(EncryptUtil.rsaEncrypt(PersistenceUtil.getUserInfo(context).getPhoneNum()), SharedPreferenceUtil.getLong(context, Constant.PARKING_LEAVE_TIME, 0));
                Call<ChargeResponse> call = chargeService.charge(chargeRequest);
                call.enqueue(new Callback<ChargeResponse>() {
                    @Override
                    public void onResponse(Call<ChargeResponse> call, Response<ChargeResponse> response) {
                        if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                            SharedPreferenceUtil.setBoolean(context, Constant.NEED_POST_LEAVE_TIME, false);
                        } else {
                            ToastUtil.showToast(context, "发送请求失败");
                            SharedPreferenceUtil.setBoolean(context, Constant.NEED_POST_LEAVE_TIME, true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ChargeResponse> call, Throwable t) {
                        ToastUtil.showToast(context, "网络连接异常");
                        SharedPreferenceUtil.setBoolean(context, Constant.NEED_POST_LEAVE_TIME, true);
                    }
                });
            }
        }else {
            LogUtil.e("NetworkChanged", "DisConnected");
        }
    }
}
