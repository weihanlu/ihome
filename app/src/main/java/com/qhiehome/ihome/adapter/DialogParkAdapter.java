package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;

import java.util.ArrayList;

public class DialogParkAdapter extends RecyclerView.Adapter<DialogParkAdapter.DialogParkHolder>{

    private Context mContext;
    private ArrayList<String> mParkingIds;
    private ArrayList<Boolean> mSelected;

    public DialogParkAdapter(Context context, ArrayList<String> parkingIds, ArrayList<Boolean> selects) {
        mContext = context;
        mParkingIds = parkingIds;
        mSelected = selects;
    }

    @Override
    public DialogParkHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_dialog_park, parent, false);
        return new DialogParkHolder(view);
    }

    @Override
    public void onBindViewHolder(final DialogParkHolder holder, final int position) {
        holder.mTvParkingId.setText(mParkingIds.get(position));
        int white = ContextCompat.getColor(mContext, R.color.white);
        int gray = ContextCompat.getColor(mContext, R.color.gray);
        holder.mTvParkingId.setTextColor(mSelected.get(position)? white: gray);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(holder.itemView, position);
                }
                for (int i = 0; i < mSelected.size(); i++) {
                    mSelected.set(i, false);
                }
                mSelected.set(holder.getLayoutPosition(), true);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mParkingIds == null? 0: mParkingIds.size();
    }

    public static class DialogParkHolder extends RecyclerView.ViewHolder {

        TextView mTvParkingId;

        private DialogParkHolder(View itemView) {
            super(itemView);
            mTvParkingId = (TextView) itemView.findViewById(R.id.tv_parking_id);
        }
    }

    public interface OnClickListener {
        void onClick(View view, int i);
    }

    public void setOnItemClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    private OnClickListener onClickListener;
}
