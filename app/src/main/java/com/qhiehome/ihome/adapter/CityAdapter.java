package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;

/**
 * Created by YueMa on 2017/9/1.
 */

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder>{

    private Context mContext;
    private String[] mCities;

    public CityAdapter(Context mContext, String[] mCities) {
        this.mContext = mContext;
        this.mCities = mCities;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CityViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_city_select, parent, false));
    }

    @Override
    public void onBindViewHolder(final CityViewHolder holder, int position) {

        holder.tv_city.setText(mCities[position]);
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
        return mCities.length;
    }

    class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tv_city;

        private CityViewHolder(View view) {
            super(view);
            tv_city = (TextView) view.findViewById(R.id.tv_city);
        }

    }

    public void setOnItemClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onClick(View view, int i);
    }
}
