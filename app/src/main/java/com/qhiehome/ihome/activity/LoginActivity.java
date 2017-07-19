package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.BindString;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends AppCompatActivity {

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

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        ActivityManager.add(this);
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
                            }
                        });
                    } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        // 提交验证码成功
                        MainActivity.start(LoginActivity.this);
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
        mHandler = new Handler();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.remove(this);
        SMSSDK.unregisterEventHandler(mEventHandler);
        mHandler.removeCallbacksAndMessages(null);
    }

    @OnClick(R.id.bt_verify)
    public void verify() {
        String phoneNum = mEtPhone.getText().toString();
        if (!TextUtils.isEmpty(phoneNum) && phoneNum.length() == DEFAULT_PHONE_LEN) {
            SMSSDK.getVerificationCode(DEFAULT_COUNTRY_CODE, phoneNum, null);
        } else {
            ToastUtil.showToast(this, login_wrongMobile);
        }
    }

    @OnClick(R.id.bt_login)
    public void login() {
        String phoneNum = mEtPhone.getText().toString();
        String verifyCode = mEtVerify.getText().toString();
        if (TextUtils.isEmpty(verifyCode)) {
            ToastUtil.showToast(this, login_emptyVerification);
        } else {
            SMSSDK.submitVerificationCode(DEFAULT_COUNTRY_CODE, phoneNum, verifyCode);
        }
    }

    private class CountDownTask implements Runnable {

        private int seconds;

        private CountDownTask(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            ToastUtil.showToast(LoginActivity.this, login_successGetVerification);
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
