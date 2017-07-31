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

public class MeAdapter extends RecyclerView.Adapter<MeAdapter.MeHolder>{

    private Context mContext;
    private String[] mTitles;

    public MeAdapter(Context context, String[] titles) {
        mContext = context;
        mTitles = titles;
    }

    @Override
    public MeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_me, parent, false);
        return new MeHolder(view);
    }

    @Override
    public void onBindViewHolder(final MeHolder holder, int position) {
        holder.mTextView.setText(mTitles[position]);
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

    public static class MeHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        private MeHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_item);
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
