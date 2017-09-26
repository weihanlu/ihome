package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;

import java.util.ArrayList;

/**
 * Created by YueMa on 2017/9/25.
 */

public class EstateInfoAdapter extends RecyclerView.Adapter<EstateInfoAdapter.EstateInfoViewHolder> {

    private Context mContext;
    private ArrayList<String> mData;
    private String[] mTitles;

    public EstateInfoAdapter() {
    }

    public EstateInfoAdapter(Context mContext, ArrayList<String> mData, String[] mTitles) {
        this.mContext = mContext;
        this.mData = mData;
        this.mTitles = mTitles;
    }

    @Override
    public EstateInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_estate_info, parent, false);
        return new EstateInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EstateInfoViewHolder holder, int position) {
        // TODO: 2017/9/25 处理显示信息
        holder.tv_data.setText(mData.get(position));
        holder.tv_title.setText(mTitles[position]);
    }

    @Override
    public int getItemCount() {
        return mTitles.length;
    }

    class EstateInfoViewHolder extends RecyclerView.ViewHolder{
        TextView tv_data;
        TextView tv_title;

        public EstateInfoViewHolder(View itemView) {
            super(itemView);
            tv_data = (TextView) itemView.findViewById(R.id.tv_estate_info_data);
            tv_title = (TextView) itemView.findViewById(R.id.tv_estate_info_title);
        }
    }
}
