package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.BLEDevice;

import java.util.ArrayList;

/**
 * the adapter used in {@link com.qhiehome.ihome.activity.BluetoothScanActivity}
 */

public class ScanLockAdapter extends RecyclerView.Adapter<ScanLockAdapter.ScanLockHolder> {

    private Context mContext;
    private ArrayList<BLEDevice> mLeDevices;

    public ScanLockAdapter(Context mContext, ArrayList<BLEDevice> mLeDevices) {
        this.mContext = mContext;
        this.mLeDevices = mLeDevices;
    }

    @Override
    public ScanLockHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ScanLockHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rv_scan_lock, parent, false));
    }

    @Override
    public void onBindViewHolder(ScanLockHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        holder.mTvLeDevice.setText(mLeDevices.get(adapterPosition).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLeDevices == null? 0: mLeDevices.size();
    }

    public static class ScanLockHolder extends RecyclerView.ViewHolder {

        TextView mTvLeDevice;

        public ScanLockHolder(View itemView) {
            super(itemView);
            mTvLeDevice = (TextView) itemView.findViewById(R.id.tv_scan_lock);
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
