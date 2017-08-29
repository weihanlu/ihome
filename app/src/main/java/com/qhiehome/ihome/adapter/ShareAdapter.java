package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;


/**
 * Created by YueMa on 2017/8/29.
 */

public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ShareViewHolder> {

    private Context mContext;

    public ShareAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public ShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_share, parent, false);
        return new ShareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShareViewHolder holder, int position) {
        switch (position){
            case 0:
                holder.ivShare.setImageResource(R.drawable.ic_share_wechat);
                holder.tvShare.setText("微信好友");
                break;
            case 1:
                holder.ivShare.setImageResource(R.drawable.ic_share_moments);
                holder.tvShare.setText("微信朋友圈");
                break;
            case 2:
                holder.ivShare.setImageResource(R.drawable.ic_share_sms);
                holder.tvShare.setText("短信");
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static class ShareViewHolder extends RecyclerView.ViewHolder{

        ImageView ivShare;
        TextView tvShare;

        public ShareViewHolder(View itemView) {
            super(itemView);
            ivShare = (ImageView) itemView.findViewById(R.id.iv_item_share);
            tvShare = (TextView) itemView.findViewById(R.id.tv_item_share);
        }
    }
}
