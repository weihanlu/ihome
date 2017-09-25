package com.qhiehome.ihome.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.JoinOwnerAdapter;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class JoinOwnerActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;
    @BindView(R.id.tv_title_toolbar)
    TextView mToolbarTitle;
    @BindView(R.id.rv_join)
    RecyclerView mRvJoin;

    @BindArray(R.array.join_title)
    String[] mTitles;
    @BindArray(R.array.join_hint)
    String[] mHints;
    @BindView(R.id.btn_join_submit)
    Button mBtnSubmit;

    private Dialog mDialog;
    private JoinOwnerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_owner);
        ButterKnife.bind(this);
        initToolbar();
        initRecyclerView();
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
        mToolbarTitle.setText("联系加盟");
    }

    private void initRecyclerView() {
        mRvJoin.setLayoutManager(new LinearLayoutManager(this));
        mRvJoin.setHasFixedSize(true);
        mAdapter = new JoinOwnerAdapter(this, mTitles, mHints);
        mRvJoin.setAdapter(mAdapter);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, JoinOwnerActivity.class);
        context.startActivity(intent);
    }

    @OnClick(R.id.btn_join_submit)
    public void onViewClicked() {
        String[] content = mAdapter.getmContent();
        mDialog = new MaterialDialog.Builder(this)
                .content(String.format("您输入的信息为 姓名：%s, 电话：%s,小区名：%s",content[0], content[1], content[2]))
                .positiveText("确认")
                .build();
        mDialog.show();
    }
}
