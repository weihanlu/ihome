package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.BLEDevice;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * BindLockAdapter recyclerView adapter {@link com.qhiehome.ihome.activity.BindLockActivity}
 */

public class BindLockAdapter extends RecyclerView.Adapter<BindLockAdapter.BindLockHolder>{

    private Context mContext;
    private ArrayList<BLEDevice> mLeDevices;

    public BindLockAdapter(Context context, ArrayList<BLEDevice> mLeDevices) {
        this.mContext = context;
        this.mLeDevices = mLeDevices;
    }

    @Override
    public BindLockHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_bind_lock, parent, false);
        return new BindLockHolder(view);
    }

    @Override
    public void onBindViewHolder(final BindLockHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLeDevices == null? 0: mLeDevices.size();
    }

    public static class BindLockHolder extends RecyclerView.ViewHolder {

        ImageView mImgView;

        private BindLockHolder(View itemView) {
            super(itemView);
            mImgView = (ImageView) itemView.findViewById(R.id.img_item);
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
