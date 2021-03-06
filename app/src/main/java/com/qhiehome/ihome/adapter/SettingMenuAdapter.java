package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;

/**
 * Me Fragment recyclerView adapter {@link com.qhiehome.ihome.fragment.MeFragment}
 */

public class SettingMenuAdapter extends RecyclerView.Adapter<SettingMenuAdapter.SettingMenuHolder>{

    private Context mContext;
    private String[] mTitles;

    public SettingMenuAdapter(Context context, String[] titles) {
        mContext = context;
        mTitles = titles;
    }

    @Override
    public SettingMenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_me, parent, false);
        return new SettingMenuHolder(view);
    }

    @Override
    public void onBindViewHolder(final SettingMenuHolder holder, int position) {
        if (position == 0) {
            holder.mTvVersion.setVisibility(View.VISIBLE);
            String[] firstLine = mTitles[position].split(";");
            holder.mTvItem.setText(firstLine[0]);
            holder.mTvVersion.setText(firstLine[1]);
        }  else {
            holder.mTvItem.setText(mTitles[position]);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(holder.getLayoutPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTitles == null? 0: mTitles.length;
    }

    public static class SettingMenuHolder extends RecyclerView.ViewHolder {

        TextView mTvItem;
        TextView mTvVersion;

        private SettingMenuHolder(View itemView) {
            super(itemView);
            mTvItem = (TextView) itemView.findViewById(R.id.tv_item);
            mTvVersion = (TextView) itemView.findViewById(R.id.tv_version);
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
