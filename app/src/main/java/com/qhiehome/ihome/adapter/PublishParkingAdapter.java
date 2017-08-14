package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.PublishBean;

import java.util.ArrayList;

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
        PublishBean requestBean = mPublishList.get(position);
        holder.mTvParkingId.setText(requestBean.getParkingId());
        holder.mTvParkingPeriod.setText("发布时间段 " + requestBean.getStartTime() + " ~ " + requestBean.getEndTime());
        holder.mIvCallbackPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onCallbackPublish(holder.itemView, holder.getLayoutPosition());
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
        notifyItemRemoved(position);
    }

    static class PublishParkingHolder extends RecyclerView.ViewHolder {

        TextView mTvParkingId;
        TextView mTvParkingPeriod;
        ImageView mIvCallbackPublish;

        private PublishParkingHolder(View itemView) {
            super(itemView);
            mTvParkingId = (TextView) itemView.findViewById(R.id.tv_parking_id);
            mTvParkingPeriod = (TextView) itemView.findViewById(R.id.tv_parking_period);
            mIvCallbackPublish = (ImageView) itemView.findViewById(R.id.iv_callback_publish);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onCallbackPublish(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    private OnItemClickListener onItemClickListener;
}
