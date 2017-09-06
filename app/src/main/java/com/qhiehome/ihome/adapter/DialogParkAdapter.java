package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.ParkingItem;

import java.util.ArrayList;

public class DialogParkAdapter extends RecyclerView.Adapter<DialogParkAdapter.DialogParkHolder>{

    private Context mContext;
    private ArrayList<ParkingItem> mParkingItems;
    private ArrayList<Boolean> mSelected;

    public DialogParkAdapter(Context context, ArrayList<ParkingItem> parkingItems, ArrayList<Boolean> selects) {
        mContext = context;
        mParkingItems = parkingItems;
        mSelected = selects;
    }

    @Override
    public DialogParkHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_dialog_park, parent, false);
        return new DialogParkHolder(view);
    }

    @Override
    public void onBindViewHolder(final DialogParkHolder holder, int position) {
        holder.mTvParkingId.setText(mParkingItems.get(position).getParkingId());
        int white = ContextCompat.getColor(mContext, R.color.white);
        int gray = ContextCompat.getColor(mContext, R.color.gray);
        holder.mTvParkingId.setTextColor(mSelected.get(position)? white: gray);
        holder.mTvLabelParkingId.setTextColor(mSelected.get(position)? white: gray);
        holder.mRlItemParking.setBackground(mSelected.get(position)?
                ContextCompat.getDrawable(mContext, R.drawable.bg_dialog_select_park): ContextCompat.getDrawable(mContext, R.drawable.bg_dialog_unselect_park));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
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
        return mParkingItems == null? 0: mParkingItems.size();
    }

    public static class DialogParkHolder extends RecyclerView.ViewHolder {

        RelativeLayout mRlItemParking;
        TextView mTvParkingId;
        TextView mTvLabelParkingId;

        private DialogParkHolder(View itemView) {
            super(itemView);
            mRlItemParking = (RelativeLayout) itemView.findViewById(R.id.ll_item_parking);
            mTvParkingId = (TextView) itemView.findViewById(R.id.tv_parking_id);
            mTvLabelParkingId = (TextView) itemView.findViewById(R.id.label_parking_id);
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
