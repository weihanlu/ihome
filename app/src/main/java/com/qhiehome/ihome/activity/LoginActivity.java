package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.persistence.UserLockBean;
import com.qhiehome.ihome.persistence.UserLockBeanDao;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.SMS.SMSResponse;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.orderusing.OrderUsingRequest;
import com.qhiehome.ihome.network.model.inquiry.orderusing.OrderUsingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;
import com.qhiehome.ihome.network.model.signin.SigninRequest;
import com.qhiehome.ihome.network.model.signin.SigninResponse;
import com.qhiehome.ihome.network.service.SMS.SMSService;
import com.qhiehome.ihome.network.service.SMS.SMSServiceGenerator;
import com.qhiehome.ihome.network.service.inquiry.OrderUsingService;
import com.qhiehome.ihome.network.service.inquiry.ParkingOwnedService;
import com.qhiehome.ihome.network.service.signin.SigninService;
import com.qhiehome.ihome.observer.SMSContentObserver;
import com.qhiehome.ihome.persistence.DaoSession;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.OrderUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitle;
    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_verify)
    EditText mEtVerify;
    @BindView(R.id.tv_specification)
    TextView mTvSpecification;
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
    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;

    private UserLockBeanDao mUserLockBeanDao;

    private static final int DEFAULT_PHONE_LEN = 11;

    private static final int UPPER_SECOND = 60;

    private static final int COUNT_DOWN_START = 0;

    public static final int GET_VERIFICATION = 3;

    private static final String SMS_KEY = "c718da9eb368b06d145a81f5661e093d";

    private static final int SUCCESS_ERROR_CODE = 0;
    @BindView(R.id.bt_login)
    Button mBtLogin;

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
        SMSContentObserver sco = new SMSContentObserver(LoginActivity.this, mHandler);
        LoginActivity.this.getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, sco);
        initView();
        initData();
    }

    private void initData() {
        DaoSession daoSession = ((IhomeApplication) getApplicationContext()).getDaoSession();
        mUserLockBeanDao = daoSession.getUserLockBeanDao();
    }

    private void initView() {
        mTvTitle.setText(R.string.login);
        SpannableString sp = new SpannableString("点击登录，表示默认同意《服务条款》");
        sp.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.theme_start_color)),
                11, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvSpecification.setText(sp);
        String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, "");
        if (!TextUtils.isEmpty(phoneNum)) {
            mEtPhone.setText(SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, ""));
            mEtVerify.requestFocus();
        }
        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @OnClick({R.id.ll_phone, R.id.rl_verify_code})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_phone:
                mEtPhone.requestFocus();
                CommonUtil.showSoftKeyboard(mEtPhone, mContext);
                break;
            case R.id.rl_verify_code:
                mEtVerify.requestFocus();
                CommonUtil.showSoftKeyboard(mEtVerify, mContext);
                break;
        }
    }

    @OnClick(R.id.tv_specification)
    public void serviceContract() {
        ServiceContractActivity.start(mContext);
    }

    private static class SMSObserverHandler extends Handler {
        private final WeakReference<LoginActivity> mActivity;

        private SMSObserverHandler(LoginActivity loginActivity) {
            mActivity = new WeakReference<>(loginActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LoginActivity loginActivity = mActivity.get();
            if (msg.what == GET_VERIFICATION) {
                loginActivity.mEtVerify.setText(msg.obj.toString());
            }
            if (msg.what == COUNT_DOWN_START) {
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
        SigninRequest signinRequest = new SigninRequest(EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.RSA));
        Call<SigninResponse> call = signinService.signin(signinRequest);
        call.enqueue(new Callback<SigninResponse>() {
            @Override
            public void onResponse(@NonNull Call<SigninResponse> call, @NonNull Response<SigninResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
//                    MainActivity.start(LoginActivity.this);
                    SharedPreferenceUtil.setString(LoginActivity.this, Constant.PHONE_KEY, mPhoneNum);
                    getOwnerParking();
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
            options.put("mobile", mPhoneNum);
            options.put("tpl_id", 41356);
            float rand = new Random().nextFloat();
            mVerification = String.valueOf(rand);
            mVerification = mVerification.substring(2, 8);
            try {
                String StrVerification = URLEncoder.encode("#code#=" + mVerification, "UTF-8");
                options.put("tpl_value", StrVerification);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            options.put("key", SMS_KEY);
            //网络请求
            SMSService smsService = SMSServiceGenerator.createService(SMSService.class);
            Call<SMSResponse> call = smsService.sendSMS(options);
            call.enqueue(new Callback<SMSResponse>() {
                @Override
                public void onResponse(Call<SMSResponse> call, Response<SMSResponse> response) {
                    SMSResponse smsResponse = response.body();
                    int error_code = smsResponse.getError_code();
                    if (error_code == SUCCESS_ERROR_CODE) {
//                        ToastUtil.showToast(LoginActivity.this,"短信发送成功");
                    } else {
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
            if (verifyCode.equals(mVerification)) {
                webLogin();
            } else {
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
            if (mHasSentSMS) {
                mBtVerify.setClickable(false);
                String leftSeconds = String.format(getResources().getString(R.string.left_second), seconds);
                mBtVerify.setText(leftSeconds);
                seconds--;
                if (seconds > 0) {
                    mHandler.postDelayed(this, 1000);
                } else if (seconds == 0) {
                    mVerification = String.valueOf(new Random().nextFloat());//1分钟后验证码失效
                    mBtVerify.setClickable(true);
                    mBtVerify.setText(login_getVerification);
                    mBtVerify.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.theme_start_color));
                }
            } else {                 //网络请求失败或者网络不通则重置按钮
                mBtVerify.setClickable(true);
                mBtVerify.setText(login_getVerification);
                mBtVerify.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

    /********重新登录时恢复用户订单数据********/
    private void getOrderInfo() {
        OrderUsingService orderUsingService = ServiceGenerator.createService(OrderUsingService.class);
        OrderUsingRequest orderUsingRequest = new OrderUsingRequest(EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.RSA));
        Call<OrderUsingResponse> call = orderUsingService.orderUsing(orderUsingRequest);
        call.enqueue(new Callback<OrderUsingResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderUsingResponse> call, @NonNull Response<OrderUsingResponse> response) {
                try {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                        OrderUsingResponse.DataBean data = response.body().getData();
                        if (data == null) {
                            MainActivity.start(LoginActivity.this);
                        } else {
                            OrderUsingResponse.DataBean.OrderBean orderBean = data.getOrder();
                            OrderUsingResponse.DataBean.EstateBean estateBean = data.getEstate();
                            OrderUtil.getInstance().setOrderInfo(mContext, orderBean.getId(), orderBean.getState(), orderBean.getStartTime(), orderBean.getEndTime(),
                                    orderBean.getParking().getName(), orderBean.getParking().getLockMac(), orderBean.getParking().getPassword(), orderBean.getParking().getGateWayId(),
                                    estateBean.getName(), estateBean.getX(), estateBean.getY());
//                            SharedPreferenceUtil.setInt(mContext, Constant.ORDER_ID, orderBean.getId());
//                            SharedPreferenceUtil.setLong(mContext, Constant.PARKING_START_TIME, orderBean.getStartTime());
//                            SharedPreferenceUtil.setLong(mContext, Constant.PARKING_END_TIME, orderBean.getEndTime());
//                            SharedPreferenceUtil.setString(mContext, Constant.RESERVE_LOCK_MAC, orderBean.getParking().getLockMac());
//                            SharedPreferenceUtil.setString(mContext, Constant.RESERVE_LOCK_PWD, orderBean.getParking().getPassword());
//                            SharedPreferenceUtil.setString(mContext, Constant.RESERVE_GATEWAY_ID, orderBean.getParking().getGateWayId());
//                            SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, orderBean.getState());
//                            SharedPreferenceUtil.setString(mContext, Constant.ESTATE_NAME, estateBean.getName());
//                            SharedPreferenceUtil.setFloat(mContext, Constant.ESTATE_LONGITUDE, (float) estateBean.getX());
//                            SharedPreferenceUtil.setFloat(mContext, Constant.ESTATE_LATITUDE, (float) estateBean.getY());
                            MainActivity.start(mContext);
                        }
                    } else {
                        ToastUtil.showToast(LoginActivity.this, "服务器繁忙，请稍后再试");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToast(LoginActivity.this, "服务器错误，请稍后再试");
                }

            }

            @Override
            public void onFailure(@NonNull Call<OrderUsingResponse> call, @NonNull Throwable t) {
                ToastUtil.showToast(LoginActivity.this, "网络连接异常");
            }
        });
    }


    /********检查用户类型：临时/业主********/
    private void getOwnerParking() {
        ParkingOwnedService parkingOwnedService = ServiceGenerator.createService(ParkingOwnedService.class);
        ParkingOwnedRequest parkingOwnedRequest = new ParkingOwnedRequest(EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.RSA));
        Call<ParkingOwnedResponse> call = parkingOwnedService.parkingOwned(parkingOwnedRequest);
        call.enqueue(new Callback<ParkingOwnedResponse>() {
            @Override
            public void onResponse(@NonNull Call<ParkingOwnedResponse> call, @NonNull Response<ParkingOwnedResponse> response) {
                try {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                        locksPersistent(response);
                        getOrderInfo();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToast(LoginActivity.this, "服务器错误，请稍后再试");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ParkingOwnedResponse> call, @NonNull Throwable t) {
                ToastUtil.showToast(LoginActivity.this, "网络连接异常");
            }
        });
    }

    /**
     * 登录时将锁所有的信息本地化
     *
     * @param response network response
     */
    private void locksPersistent(@NonNull Response<ParkingOwnedResponse> response) {
        mUserLockBeanDao.deleteAll();
        List<ParkingResponse.DataBean.EstateBean> estateList = response.body().getData().getEstate();
        if (estateList != null) {
            if (estateList.size() == 0) {
                SharedPreferenceUtil.setInt(LoginActivity.this, Constant.USER_TYPE, Constant.USER_TYPE_TEMP);
            } else {
                SharedPreferenceUtil.setInt(LoginActivity.this, Constant.USER_TYPE, Constant.USER_TYPE_OWNER);
                UserLockBean userLockBean;
                for (ParkingResponse.DataBean.EstateBean estate : estateList) {
                    for (ParkingResponse.DataBean.EstateBean.ParkingListBean parkingBean : estate.getParkingList()) {
                        userLockBean = new UserLockBean(null, estate.getName(), parkingBean.getName(), parkingBean.getId(), parkingBean.getGatewayId(),
                                parkingBean.getLockMac(), parkingBean.getPassword(), false);
                        LogUtil.d(TAG, userLockBean.toString());
                        mUserLockBeanDao.insertOrReplace(userLockBean);
                    }
                }
            }
        }
    }

}
