package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        holder.mTvParkingId.setText(publishBean.getParkingId());
        holder.mTvParkingPeriod.setText(publishBean.getStartTime() + " - " + publishBean.getEndTime());
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

    public static class PublishParkingHolder extends RecyclerView.ViewHolder {

        TextView mTvParkingId;
        TextView mTvParkingPeriod;
        SwitchCompat mScRepublish;
        TextView mTvRepublishDate;
        TextView mTvItemDelete;
        public LinearLayout mLayout;

        private PublishParkingHolder(View itemView) {
            super(itemView);
            mTvParkingId = (TextView) itemView.findViewById(R.id.tv_parking_id);
            mTvParkingPeriod = (TextView) itemView.findViewById(R.id.tv_parking_period);
            mScRepublish = (SwitchCompat) itemView.findViewById(R.id.sc_republish);
            mTvRepublishDate = (TextView) itemView.findViewById(R.id.tv_date_selected);
            mTvItemDelete = (TextView) itemView.findViewById(R.id.tv_item_delete);
            mLayout = (LinearLayout) itemView.findViewById(R.id.item_layout);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onCallbackPublish(View view, int position);
        void onToggleRepublish(View switcher, boolean isChecked, int position, TextView textView);
    }

}
