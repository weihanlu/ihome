package com.qhiehome.ihome.activity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.AboutMenuAdapter;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.ToastUtil;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.phoneNumber;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.toolbar_center)
    Toolbar mTbAbout;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvToolbarTitle;
    @BindView(R.id.rv_about)
    RecyclerView mRvAbout;
    @BindArray(R.array.about_menu_title)
    String[] mTitles;
    @BindArray(R.array.about_menu_info)
    String[] mInfo;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtil.setStatusBarGradient(this);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        mContext = this;
        initToolbar();
        initRecyclerView();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    private void initToolbar() {
        setSupportActionBar(mTbAbout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbAbout.setTitle("");
        mTbAbout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvToolbarTitle.setText("关于");
    }

    private void initRecyclerView(){
        mRvAbout.setLayoutManager(new LinearLayoutManager(this));
        mRvAbout.setHasFixedSize(true);
        mRvAbout.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        AboutMenuAdapter aboutMenuAdapter = new AboutMenuAdapter(this, mTitles, mInfo);
        initListener(aboutMenuAdapter);
        mRvAbout.setAdapter(aboutMenuAdapter);
    }

    private void initListener(AboutMenuAdapter aboutMenuAdapter) {
        aboutMenuAdapter.setOnItemClickListener(new AboutMenuAdapter.OnClickListener() {
            @Override
            public void onClick(int i) {
                switch (i) {
                    case 0://微信公众号
                        ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        cmb.setText(mInfo[0]);
                        ToastUtil.showToast(mContext, "已复制到剪贴板");
                        break;
                    case 1://官方网站
                        OfficialWebActivity.start(mContext);
                        break;
                    case 2://客服热线
                        Intent dialIntent =  new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mInfo[2]));//跳转到拨号界面，同时传递电话号码
                        startActivity(dialIntent);
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
