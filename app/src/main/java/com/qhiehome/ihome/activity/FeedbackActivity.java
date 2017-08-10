package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedbackActivity extends AppCompatActivity {

    @BindView(R.id.tb_feedback)
    Toolbar mTbFeedback;
    @BindView(R.id.ev_feedback)
    EditText mEvFeedback;
    @BindView(R.id.btn_feedback)
    Button mBtnFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        initToolbar();
        mEvFeedback.setFocusable(true);
        mEvFeedback.setFocusableInTouchMode(true);
    }

    @OnClick(R.id.btn_feedback)
    public void onViewClicked() {
        if (mEvFeedback.getText().toString().trim().isEmpty()){
            ToastUtil.showToast(this, "请输入反馈内容");
        }else {
            // TODO: 2017/8/9 意见反馈接口
        }
    }

    private void initToolbar(){
        setSupportActionBar(mTbFeedback);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbFeedback.setTitle("意见反馈");
        mTbFeedback.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mTbFeedback.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, FeedbackActivity.class);
        context.startActivity(intent);
    }
}
