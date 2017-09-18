package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.os.HandlerThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;

/**
 * Created by YueMa on 2017/9/5.
 */

public class AboutMenuAdapter extends RecyclerView.Adapter<AboutMenuAdapter.AboutViewHolder> {

    private Context mContext;
    private String[] mTitles;
    private String[] mInfo;

    public AboutMenuAdapter(Context mContext, String[] mTitles, String[] mInfo) {
        this.mContext = mContext;
        this.mTitles = mTitles;
        this.mInfo = mInfo;
    }

    @Override
    public AboutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_me, parent, false);
        return new AboutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AboutViewHolder holder, int position) {
        holder.mTvTitle.setText(mTitles[position]);
        holder.mTvInfo.setText(mInfo[position]);
        holder.mTvInfo.setVisibility(View.VISIBLE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(holder.getLayoutPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTitles.length;
    }

    public static class AboutViewHolder extends RecyclerView.ViewHolder{

        TextView mTvTitle;
        TextView mTvInfo;

        public AboutViewHolder(View itemView) {
            super(itemView);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_item);
            mTvInfo = (TextView) itemView.findViewById(R.id.tv_version);
        }
    }

    public interface OnClickListener {
        void onClick(int i);
    }

    public void setOnItemClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    private OnClickListener onClickListener;
}
