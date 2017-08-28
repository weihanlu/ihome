package com.qhiehome.ihome.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingRequest;
import com.qhiehome.ihome.network.model.park.enter.EnterParkingResponse;
import com.qhiehome.ihome.network.service.park.EnterParkingService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.NetworkUtils;
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
            //需要POST停车请求
            if (SharedPreferenceUtil.getBoolean(context, Constant.NEED_POST_ENTER_TIME, false)){
                EnterParkingService enterParkingService = ServiceGenerator.createService(EnterParkingService.class);
                EnterParkingRequest enterParkingRequest = new EnterParkingRequest(SharedPreferenceUtil.getString(context, Constant.PHONE_KEY, ""), SharedPreferenceUtil.getLong(context, Constant.PARKING_ENTER_TIME, 0));
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
            // TODO: 2017/8/25 离开请求
        }

//        System.out.println("网络状态发生变化");
//        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
//
//            //获得ConnectivityManager对象
//            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            //获取ConnectivityManager对象对应的NetworkInfo对象
//            //获取WIFI连接的信息
//            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//            //获取移动数据连接的信息
//            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
//                networkConnected = true;
//            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
//                networkConnected = true;
//            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
//                networkConnected = true;
//            } else {
//                Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
//                networkConnected = false;
//            }
////API大于23时使用下面的方式进行网络监听
//        }else {
//
//            System.out.println("API level 大于23");
//            //获得ConnectivityManager对象
//            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            //获取所有网络连接的信息
//            Network[] networks = connMgr.getAllNetworks();
//            //用于存放网络连接信息
//            StringBuilder sb = new StringBuilder();
//            //通过循环将网络信息逐个取出来
//            for (int i=0; i < networks.length; i++){
//                //获取ConnectivityManager对象对应的NetworkInfo对象
//                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
//                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
//            }
//            Toast.makeText(context, sb.toString(),Toast.LENGTH_SHORT).show();
//        }
    }
}
