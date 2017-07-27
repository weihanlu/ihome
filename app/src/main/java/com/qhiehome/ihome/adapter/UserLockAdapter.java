package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.BLEDevice;
import com.qhiehome.ihome.bean.UserLockBean;

import java.util.ArrayList;

public class UserLockAdapter extends RecyclerView.Adapter<UserLockAdapter.UserLockHolder> {

    private Context mContext;
    private ArrayList<UserLockBean> mUserLocks;

    public UserLockAdapter(Context mContext, ArrayList<UserLockBean> mUserLocks) {
        this.mContext = mContext;
        this.mUserLocks = mUserLocks;
    }

    @Override
    public UserLockHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserLockHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rv_lock, parent, false));
    }

    @Override
    public void onBindViewHolder(UserLockHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        UserLockBean userLockBean = mUserLocks.get(adapterPosition);
        holder.mTvLockName.setText(userLockBean.getLockName());
        holder.mTvLockEstateName.setText(userLockBean.getLockEstateName());
        boolean rented = userLockBean.isRented();
        holder.mTvRentalStatus.setText(rented? "已租用": "可使用");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserLocks == null? 0: mUserLocks.size();
    }

    public static class UserLockHolder extends RecyclerView.ViewHolder {

        TextView mTvLockName;
        TextView mTvLockEstateName;
        TextView mTvRentalStatus;

        public UserLockHolder(View itemView) {
            super(itemView);
            mTvLockName = (TextView) itemView.findViewById(R.id.tv_lock_name);
            mTvLockEstateName = (TextView) itemView.findViewById(R.id.tv_lock_estate_name);
            mTvRentalStatus = (TextView) itemView.findViewById(R.id.tv_rental_status);
        }
    }

    public interface OnClickListener {
        void onClick(int i);
    }

    public void setOnItemClickListener(ScanLockAdapter.OnClickListener listener) {
        this.onClickListener = listener;
    }

    private ScanLockAdapter.OnClickListener onClickListener;
}
