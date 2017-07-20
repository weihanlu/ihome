package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParkingListActivity extends AppCompatActivity {


    @BindView(R.id.tb_parking)
    Toolbar mTbParking;
    @BindView(R.id.rv_parking)
    RecyclerView mRvParking;
    @BindView(R.id.tv_estate_name)
    TextView mTvEstateName;
    private ParkingAdapter mAdapter;
    private List<Map<String, String>> parking_data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_list);
        ButterKnife.bind(this);
        initToolbar();
        initData();
        initRecyclerView();

    }

    private void initToolbar() {
        setSupportActionBar(mTbParking);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbParking.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = this.getIntent();
        String estate_name = intent.getStringExtra("name");
        mTvEstateName.setText(estate_name);
    }

    private void initData() {
        parking_data.add(new HashMap<String, String>() {{
            put("name", "Jack");
            put("time_start", "8");
            put("time_end", "10");
        }});
        parking_data.add(new HashMap<String, String>() {{
            put("name", "Jerry");
            put("time_start", "9");
            put("time_end", "14");
        }});
        parking_data.add(new HashMap<String, String>() {{
            put("name", "Tom");
            put("time_start", "10");
            put("time_end", "13");
        }});
    }

    private void initRecyclerView() {
        mRvParking.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ParkingAdapter();
        mRvParking.setAdapter(mAdapter);
        Context context = ParkingListActivity.this;
        DividerItemDecoration did = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        mRvParking.addItemDecoration(did);
    }

    class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(ParkingListActivity.this).inflate(R.layout.item_parking_list, parent, false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tv_name.setText(parking_data.get(position).get("name"));
            holder.tv_time.setText(parking_data.get(position).get("time_start") + "~" + parking_data.get(position).get("time_end"));
        }


        @Override
        public int getItemCount() {
            return parking_data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name;
            TextView tv_time;

            public MyViewHolder(View view) {
                super(view);
                tv_name = (TextView) view.findViewById(R.id.tv_parking_name);
                tv_time = (TextView) view.findViewById(R.id.tv_parking_time);
            }

        }
    }
}