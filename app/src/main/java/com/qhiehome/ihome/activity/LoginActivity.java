package com.qhiehome.ihome.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.signin.SigninRequest;
import com.qhiehome.ihome.network.model.signin.SigninResponse;
import com.qhiehome.ihome.network.service.signin.SigninService;
import com.qhiehome.ihome.observer.SMSContentObserver;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;


import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.BindString;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
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

    private EventHandler mEventHandler;

    private static final int DEFAULT_PHONE_LEN = 11;

    private static final String DEFAULT_COUNTRY_CODE = "86";

    private static final int UPPER_SECOND = 60;

    public static final int GET_VERIFICATION = 3;

    private Handler mHandler;

    private String mPhoneNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mEventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        // 获取验证码成功
                        // count down
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CountDownTask countDownTask = new CountDownTask(UPPER_SECOND);
                                countDownTask.run();
                                CommonUtil.hideKeyboard(LoginActivity.this);
                            }
                        });
                    } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        // 验证码验证成功 + 同时向线上发送请求说明用户已登录后台
                        webLogin();
                    }
                } else {
                    LogUtil.d(TAG, ((Throwable) data).getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(LoginActivity.this, login_wrongVerification);
                        }
                    });
                }
            }
        };
        // 注册监听器
        SMSSDK.registerEventHandler(mEventHandler);
        mHandler = new SMSObserverHandler(this);
        SMSContentObserver sco = new SMSContentObserver(LoginActivity.this,mHandler);
        LoginActivity.this.getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, sco);
    }

    private static class SMSObserverHandler extends Handler{
        private final WeakReference<LoginActivity> mActivity;
        private SMSObserverHandler(LoginActivity loginActivity){
            mActivity = new WeakReference<LoginActivity>(loginActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            LoginActivity loginActivity = mActivity.get();
            if(msg.what == GET_VERIFICATION){
                loginActivity.mEtVerify.setText(msg.obj.toString());
            }
        }
    }

    private void webLogin() {
        SigninService signinService = ServiceGenerator.createService(SigninService.class);
        SigninRequest signinRequest = new SigninRequest(EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.SHA_256));
        Call<SigninResponse> call = signinService.signin(signinRequest);
        call.enqueue(new Callback<SigninResponse>() {
            @Override
            public void onResponse(Call<SigninResponse> call, Response<SigninResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    MainActivity.start(LoginActivity.this);
                    SharedPreferenceUtil.setString(LoginActivity.this, Constant.PHONE_KEY, mPhoneNum);
                }
            }
            @Override
            public void onFailure(Call<SigninResponse> call, Throwable t) {
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
        SMSSDK.unregisterEventHandler(mEventHandler);
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
            SMSSDK.getVerificationCode(DEFAULT_COUNTRY_CODE, mPhoneNum, null);
        } else {
            ToastUtil.showToast(this, login_wrongMobile);
        }
    }

    @OnClick(R.id.bt_login)
    public void login() {
        mPhoneNum = mEtPhone.getText().toString();
        String verifyCode = mEtVerify.getText().toString();
        if (TextUtils.isEmpty(verifyCode)) {
            ToastUtil.showToast(this, login_emptyVerification);
        } else {
            SMSSDK.submitVerificationCode(DEFAULT_COUNTRY_CODE, mPhoneNum, verifyCode);
        }
    }

    private class CountDownTask implements Runnable {

        private int seconds;

        private CountDownTask(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            mBtVerify.setClickable(false);
            String leftSeconds = String.format(getResources().getString(R.string.left_second), seconds);
            mBtVerify.setText(leftSeconds);
            mBtVerify.setTextColor(getResources().getColor(R.color.colorAccent));
            seconds--;
            if (seconds > 0) {
                mHandler.postDelayed(this, 1000);
            } else if (seconds == 0) {
                mBtVerify.setClickable(true);
                mBtVerify.setText(login_getVerification);
                mBtVerify.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }


}
