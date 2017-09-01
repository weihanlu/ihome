package com.qhiehome.ihome.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.CityAdapter;
import com.qhiehome.ihome.util.CommonUtil;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CityActivity extends AppCompatActivity {


    @BindView(R.id.rv_city)
    RecyclerView mRvCity;
    @BindArray(R.array.cities)
    String[] mCities;
    @BindView(R.id.toolbar_center)
    Toolbar mTbCity;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitleToolbar;
    @BindView(R.id.tv_select_city_current)
    TextView mTvCurrentCity;

    private String mCurrentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtil.setStatusBarGradient(this);
        setContentView(R.layout.activity_city);
        ButterKnife.bind(this);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mCurrentCity = bundle.getString("city");
        mTvCurrentCity.setText(mCurrentCity);
        initToolbar();
        initRecyclerView();
    }


    private void initToolbar() {
        setSupportActionBar(mTbCity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbCity.setTitle("");
        mTvTitleToolbar.setText("选择城市");
        mTbCity.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        mRvCity.setLayoutManager(new LinearLayoutManager(this));
        CityAdapter adapter = new CityAdapter(this, mCities);
        adapter.setOnItemClickListener(new CityAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int i) {
                SelectCity(mCities[i]);
            }
        });
        mRvCity.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRvCity.setAdapter(adapter);
    }

    @OnClick(R.id.layout_select_city_current)
    public void onViewClicked() {
        SelectCity(mCurrentCity);
    }


    private void SelectCity(String cityName){
        Bundle bundle = new Bundle();
        bundle.putString("city", cityName);
        Intent backIntent = new Intent();
        backIntent.putExtras(bundle);
        setResult(RESULT_OK, backIntent);
        finish();
    }
}
