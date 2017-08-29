package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.PublishBean;

import java.util.ArrayList;
import java.util.Collections;

public class PublishParkingAdapter extends RecyclerView.Adapter<PublishParkingAdapter.PublishParkingHolder>{

    private Context mContext;
    private ArrayList<PublishBean> mPublishList;

    public PublishParkingAdapter(Context context, ArrayList<PublishBean> mPublishList) {
        this.mContext = context;
        this.mPublishList = mPublishList;
    }

    @Override
    public PublishParkingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_publish_parking, parent, false);
        return new PublishParkingHolder(view);
    }

    @Override
    public void onBindViewHolder(final PublishParkingHolder holder, final int position) {
        PublishBean publishBean = mPublishList.get(position);
        holder.mTvParkingId.setText("车位号：" + publishBean.getParkingId());
        holder.mTvParkingPeriod.setText("发布时间段 " + publishBean.getStartTime() + " ~ " + publishBean.getEndTime());
        holder.mIvCallbackPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onCallbackPublish(holder.itemView, holder.getLayoutPosition());
                }
            }
        });
        holder.mScRepublish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (onItemClickListener != null) {
                    onItemClickListener.onToggleRepublish(buttonView, isChecked, holder.getLayoutPosition(), holder.mTvRepublishDate);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPublishList == null? 0: mPublishList.size();
    }

    public void addItem(PublishBean publishBean, int position) {
        mPublishList.add(position, publishBean);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mPublishList.remove(position);
        Collections.sort(mPublishList);
        notifyItemRemoved(position);
    }

    static class PublishParkingHolder extends RecyclerView.ViewHolder {

        TextView mTvParkingId;
        TextView mTvParkingPeriod;
        ImageView mIvCallbackPublish;
        SwitchCompat mScRepublish;
        TextView mTvRepublishDate;

        private PublishParkingHolder(View itemView) {
            super(itemView);
            mTvParkingId = (TextView) itemView.findViewById(R.id.tv_parking_id);
            mTvParkingPeriod = (TextView) itemView.findViewById(R.id.tv_parking_period);
            mIvCallbackPublish = (ImageView) itemView.findViewById(R.id.iv_callback_publish);
            mScRepublish = (SwitchCompat) itemView.findViewById(R.id.sc_republish);
            mTvRepublishDate = (TextView) itemView.findViewById(R.id.tv_date_selected);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onCallbackPublish(View view, int position);
        void onToggleRepublish(View view, boolean isChecked, int position, TextView textView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    private OnItemClickListener onItemClickListener;
}
