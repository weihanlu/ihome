package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.feedback.FeedbackRequest;
import com.qhiehome.ihome.network.model.feedback.FeedbackResponse;
import com.qhiehome.ihome.network.service.feedback.FeedbackService;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends AppCompatActivity {


    @BindView(R.id.et_feedback)
    EditText mEtFeedback;
    @BindView(R.id.btn_feedback)
    Button mBtnFeedback;
    @BindView(R.id.toolbar_center)
    Toolbar mTbFeedback;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitleToolbar;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtil.setStatusBarGradient(this);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        initToolbar();
        mContext = this;
    }

    @OnClick(R.id.btn_feedback)
    public void onViewClicked() {
        String advice = mEtFeedback.getText().toString();
        if (TextUtils.isEmpty(advice)) {
            ToastUtil.showToast(this, "请输入反馈内容");
        } else {
            FeedbackService feedbackService = ServiceGenerator.createService(FeedbackService.class);
            FeedbackRequest feedbackRequest = new FeedbackRequest(advice);
            Call<FeedbackResponse> call = feedbackService.sendFeedback(feedbackRequest);
            call.enqueue(new Callback<FeedbackResponse>() {
                @Override
                public void onResponse(@NonNull Call<FeedbackResponse> call, @NonNull Response<FeedbackResponse> response) {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(mContext, "意见发送成功");
                                mEtFeedback.setText("");
                                CommonUtil.hideKeyboard(FeedbackActivity.this);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FeedbackResponse> call, @NonNull Throwable t) {

                }
            });
        }
    }

    private void initToolbar() {
        setSupportActionBar(mTbFeedback);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbFeedback.setTitle("");
        mTvTitleToolbar.setText("意见反馈");
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

    @OnClick(R.id.fl_feedback)
    public void onFeedbackClick() {
        mEtFeedback.requestFocus();
        CommonUtil.toggleKeyboard(this);
    }
}
