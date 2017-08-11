package com.qhiehome.ihome.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CityActivity extends AppCompatActivity {

    @BindView(R.id.tb_city)
    Toolbar mTbCity;
    @BindView(R.id.rv_city)
    RecyclerView mRvCity;

    private List<String> mCities = new ArrayList<>();
    private String mCurrentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        ButterKnife.bind(this);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mCurrentCity = bundle.getString("city");
        initCities();
        initToolbar();
        initRecyclerView();
    }

    private void initCities(){
        mCities.add("北京市");
        mCities.add("上海市");
        mCities.add("广州市");
        mCities.add("深圳市");
        mCities.add("杭州市");
    }

    private void initToolbar() {
        setSupportActionBar(mTbCity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbCity.setTitle("选择城市");
        mTbCity.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mTbCity.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView(){
        mRvCity.setLayoutManager(new LinearLayoutManager(this));
        CityAdapter adapter = new CityAdapter();
        adapter.setOnItemClickListener(new OnClickListener() {
            @Override
            public void onClick(View view, int i) {
                String city_selected;
                if (i == 0){
                    city_selected = mCurrentCity;
                }else {
                    city_selected = mCities.get(i-1);
                }

                Bundle bundle = new Bundle();
                bundle.putString("city", city_selected);
                Intent backIntent = new Intent();
                backIntent.putExtras(bundle);
                setResult(RESULT_OK, backIntent);
                finish();
            }
        });
        mRvCity.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRvCity.setAdapter(adapter);
    }

    class CityAdapter extends RecyclerView.Adapter<CityAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(CityActivity.this).inflate(R.layout.item_city_select, parent, false));
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            if (position == 0){
                holder.tv_city.setText(mCurrentCity);
                holder.tv_hint.setText("当前城市");
            }else {
                holder.tv_city.setText(mCities.get(position-1));
                holder.tv_hint.setText("");
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return mCities.size() + 1;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_city;
            TextView tv_hint;


            private MyViewHolder(View view) {
                super(view);
                tv_city = (TextView) view.findViewById(R.id.tv_city);
                tv_hint = (TextView) view.findViewById(R.id.tv_city_hint);
            }

        }

        public void setOnItemClickListener(OnClickListener listener) {
            this.onClickListener = listener;
        }

        private OnClickListener onClickListener;

    }

    public interface OnClickListener {
        void onClick(View view, int i);
    }
}
