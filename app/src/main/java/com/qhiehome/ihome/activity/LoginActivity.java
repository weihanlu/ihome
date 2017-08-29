package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.SMS.SMSResponse;
import com.qhiehome.ihome.network.model.inquiry.orderusing.OrderUsingRequest;
import com.qhiehome.ihome.network.model.inquiry.orderusing.OrderUsingResponse;
import com.qhiehome.ihome.network.model.park.reserve.ReserveResponse;
import com.qhiehome.ihome.network.model.signin.SigninRequest;
import com.qhiehome.ihome.network.model.signin.SigninResponse;
import com.qhiehome.ihome.network.service.SMS.SMSService;
import com.qhiehome.ihome.network.service.SMS.SMSServiceGenerator;
import com.qhiehome.ihome.network.service.inquiry.OrderUsingService;
import com.qhiehome.ihome.network.service.signin.SigninService;
import com.qhiehome.ihome.observer.SMSContentObserver;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;


import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.BindString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_vertify)
    EditText mEtVerify;
    @BindView(R.id.bt_verify)
    Button mBtVerify;
    @BindString(R.string.login_emptyVerification)
    String login_emptyVerification;
    @BindString(R.string.login_wrongMobile)
    String login_wrongMobile;
    @BindString(R.string.login_wrongVerification)
    String login_wrongVerification;
    @BindString(R.string.login_getVerification)
    String login_getVerification;
    @BindString(R.string.login_successGetVerification)
    String login_successGetVerification;

    private static final int DEFAULT_PHONE_LEN = 11;

    private static final int UPPER_SECOND = 60;

    private static final int COUNT_DOWN_START = 0;

    public static final int GET_VERIFICATION = 3;

    private static final String SMS_KEY = "c718da9eb368b06d145a81f5661e093d";

    private static final int SUCCESS_ERROR_CODE = 0;

    private Handler mHandler;

    private String mPhoneNum;

    private String mVerification;

    private boolean mHasSentSMS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mHandler = new SMSObserverHandler(this);
        SMSContentObserver sco = new SMSContentObserver(LoginActivity.this,mHandler);
        LoginActivity.this.getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, sco);
        initView();
    }

    private void initView() {
        String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, "");
        if (!TextUtils.isEmpty(phoneNum)) {
            mEtPhone.setText(SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, ""));
            mEtVerify.requestFocus();
        }
    }

    private static class SMSObserverHandler extends Handler{
        private final WeakReference<LoginActivity> mActivity;
        private SMSObserverHandler(LoginActivity loginActivity){
            mActivity = new WeakReference<>(loginActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            final LoginActivity loginActivity = mActivity.get();
            if(msg.what == GET_VERIFICATION){
                loginActivity.mEtVerify.setText(msg.obj.toString());
            }
            if (msg.what == COUNT_DOWN_START){
                loginActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CountDownTask countDownTask = loginActivity.new CountDownTask(UPPER_SECOND);
                        countDownTask.run();
                        CommonUtil.hideKeyboard(loginActivity);
                    }
                });
            }
        }
    }



    private void webLogin() {
        SigninService signinService = ServiceGenerator.createService(SigninService.class);
        SigninRequest signinRequest = new SigninRequest(EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.SHA_256));
        Call<SigninResponse> call = signinService.signin(signinRequest);
        call.enqueue(new Callback<SigninResponse>() {
            @Override
            public void onResponse(@NonNull Call<SigninResponse> call, @NonNull Response<SigninResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
//                    MainActivity.start(LoginActivity.this);
                    SharedPreferenceUtil.setString(LoginActivity.this, Constant.PHONE_KEY, mPhoneNum);
                    getOrderInfo();
                }
            }
            @Override
            public void onFailure(@NonNull Call<SigninResponse> call, @NonNull Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(LoginActivity.this, "网络连接异常");
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @OnClick(R.id.bt_verify)
    public void verify() {
        mPhoneNum = mEtPhone.getText().toString();
        if (!TextUtils.isEmpty(mPhoneNum) && mPhoneNum.length() == DEFAULT_PHONE_LEN) {
            mEtVerify.setText("");
            //获取验证码
            mHasSentSMS = true;
            mHandler.sendEmptyMessage(COUNT_DOWN_START);
            //请求参数
            Map<String, Object> options = new HashMap<>();
            options.put("mobile",mPhoneNum);
            options.put("tpl_id",41356);
            float rand = new Random().nextFloat();
            mVerification = String.valueOf(rand);
            mVerification = mVerification.substring(2, 8);
            try{
                String StrVerification = URLEncoder.encode("#code#=" + mVerification,"UTF-8");
                options.put("tpl_value",StrVerification);
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            options.put("key",SMS_KEY);
            //网络请求
            SMSService smsService = SMSServiceGenerator.createService(SMSService.class);
            Call<SMSResponse> call = smsService.sendSMS(options);
            call.enqueue(new Callback<SMSResponse>() {
                @Override
                public void onResponse(Call<SMSResponse> call, Response<SMSResponse> response) {
                    SMSResponse smsResponse = response.body();
                    int error_code = smsResponse.getError_code();
                    if (error_code == SUCCESS_ERROR_CODE){
//                        ToastUtil.showToast(LoginActivity.this,"短信发送成功");
                    }else {
                        ToastUtil.showToast(LoginActivity.this, smsResponse.getReason());
                        mHasSentSMS = false;
                    }
                }
                @Override
                public void onFailure(Call<SMSResponse> call, Throwable t) {
                    ToastUtil.showToast(LoginActivity.this, "网络异常");
                    mHasSentSMS = false;
                }
            });
        } else {
            ToastUtil.showToast(this, login_wrongMobile);
        }
    }

    @OnClick(R.id.bt_login)
    public void login() {
        LogUtil.d(TAG, "login() executed");
        mPhoneNum = mEtPhone.getText().toString();
        String verifyCode = mEtVerify.getText().toString();
        if (TextUtils.isEmpty(verifyCode)) {
            ToastUtil.showToast(this, login_emptyVerification);
        } else {
            //验证验证码
            //SMSSDK.submitVerificationCode(DEFAULT_COUNTRY_CODE, mPhoneNum, verifyCode);
            if (verifyCode.equals(mVerification)){
                webLogin();
            }else {
                ToastUtil.showToast(this, login_wrongVerification);
            }
        }
    }

    private class CountDownTask implements Runnable {

        private int seconds;

        private CountDownTask(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            if (mHasSentSMS){
                mBtVerify.setClickable(false);
                String leftSeconds = String.format(getResources().getString(R.string.left_second), seconds);
                mBtVerify.setText(leftSeconds);
                mBtVerify.setTextColor(getResources().getColor(R.color.colorAccent));
                seconds--;
                if (seconds > 0) {
                    mHandler.postDelayed(this, 1000);
                } else if (seconds == 0) {
                    mVerification = String.valueOf(new Random().nextFloat());//1分钟后验证码失效
                    mBtVerify.setClickable(true);
                    mBtVerify.setText(login_getVerification);
                    mBtVerify.setTextColor(getResources().getColor(R.color.black));
                }
            }else {                 //网络请求失败或者网络不通则重置按钮
                mBtVerify.setClickable(true);
                mBtVerify.setText(login_getVerification);
                mBtVerify.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

    /********重新登录时恢复用户订单数据********/
    private void getOrderInfo(){
        OrderUsingService orderUsingService = ServiceGenerator.createService(OrderUsingService.class);
        OrderUsingRequest orderUsingRequest = new OrderUsingRequest(EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.SHA_256));
        Call<OrderUsingResponse> call = orderUsingService.orderUsing(orderUsingRequest);
        call.enqueue(new Callback<OrderUsingResponse>() {
            @Override
            public void onResponse(Call<OrderUsingResponse> call, Response<OrderUsingResponse> response) {
                try {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE){
                        if (response.body().getData().getOrder() == null && response.body().getData().getEstate() == null){
                            MainActivity.start(LoginActivity.this);
                        }else {
                            OrderUsingResponse.DataBean.OrderBean orderBean = response.body().getData().getOrder();
                            OrderUsingResponse.DataBean.EstateBean estateBean = response.body().getData().getEstate();
                            SharedPreferenceUtil.setLong(LoginActivity.this, Constant.PARKING_START_TIME, orderBean.getStartTime());
                            SharedPreferenceUtil.setLong(LoginActivity.this, Constant.PARKING_END_TIME, orderBean.getEndTime());
                            SharedPreferenceUtil.setString(LoginActivity.this, Constant.RESERVE_LOCK_MAC, orderBean.getParking().getLockMac());
                            SharedPreferenceUtil.setString(LoginActivity.this, Constant.RESERVE_LOCK_PWD, orderBean.getParking().getPassword());
                            SharedPreferenceUtil.setString(LoginActivity.this, Constant.RESERVE_GATEWAY_ID, orderBean.getParking().getGateWayId());
                            SharedPreferenceUtil.setInt(LoginActivity.this, Constant.ORDER_STATE, orderBean.getState());
                            SharedPreferenceUtil.setString(LoginActivity.this, Constant.ESTATE_NAME, estateBean.getName());
                            SharedPreferenceUtil.setFloat(LoginActivity.this, Constant.ESTATE_LONGITUDE, (float) estateBean.getX());
                            SharedPreferenceUtil.setFloat(LoginActivity.this, Constant.ESTATE_LATITUDE, (float) estateBean.getY());
                            MainActivity.start(LoginActivity.this);
                        }
                    }else {
                        ToastUtil.showToast(LoginActivity.this, "服务器繁忙，请稍后再试");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtil.showToast(LoginActivity.this, "服务器错误，请稍后再试");
                }

            }

            @Override
            public void onFailure(Call<OrderUsingResponse> call, Throwable t) {
                ToastUtil.showToast(LoginActivity.this, "网络连接异常");
            }
        });
    }

}
