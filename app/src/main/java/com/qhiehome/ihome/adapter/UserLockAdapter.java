package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.UserLockBean;
import com.qhiehome.ihome.util.ToastUtil;

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
    public void onBindViewHolder(final UserLockHolder holder, final int position) {
        UserLockBean userLockBean = mUserLocks.get(position);
        holder.mTvLockName.setText(userLockBean.getParkingName());
        holder.mTvLockEstateName.setText(userLockBean.getLockEstateName());
        boolean isRented = userLockBean.isRented();
        holder.mTvRentalStatus.setText(isRented? "已租用": "可使用");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                }
            }
        });
        holder.mBtModifyPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onButtonClick(holder.mBtModifyPwd, holder.getLayoutPosition());
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
        Button mBtModifyPwd;

        public UserLockHolder(View itemView) {
            super(itemView);
            mTvLockName = (TextView) itemView.findViewById(R.id.tv_lock_name);
            mTvLockEstateName = (TextView) itemView.findViewById(R.id.tv_lock_estate_name);
            mTvRentalStatus = (TextView) itemView.findViewById(R.id.tv_rental_status);
            mBtModifyPwd = (Button) itemView.findViewById(R.id.bt_modify_pwd);
        }
    }

    public interface OnItemClickListener {
        void onClick(View view, int i);
        void onButtonClick(View view, int i);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    private OnItemClickListener onItemClickListener;
}
