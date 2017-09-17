package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.model.inquiry.orderowner.OrderOwnerResponse;
import com.qhiehome.ihome.util.Constant;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by YueMa on 2017/8/30.
 */

public class OrderOwnerAdapter extends RecyclerView.Adapter<OrderOwnerAdapter.OrderOwnerViewHolder>{

    private Context mContext;
    private List<OrderOwnerResponse.DataBean.OrderListBean> mOrderBeanList;

    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final String DECIMAL_2 = "¥%.2f";


    public OrderOwnerAdapter(Context mContext, List<OrderOwnerResponse.DataBean.OrderListBean> mOrderBeanList) {
        this.mContext = mContext;
        this.mOrderBeanList = mOrderBeanList;
    }

    @Override
    public OrderOwnerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_order_owner, parent, false);

        return new OrderOwnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderOwnerViewHolder holder, int position) {
        OrderOwnerResponse.DataBean.OrderListBean orderListBean = mOrderBeanList.get(position);

        String parkingName = orderListBean.getEstate().getName();
        holder.tvParkingName.setText(parkingName);

        int orderId = orderListBean.getId();
        holder.tvOrderId.setText(String.format(Locale.CHINA, mContext.getString(R.string.order_owner_orderId), orderId));

        String time = "";
        String fee = "";
        switch (mOrderBeanList.get(position).getState()){
            case Constant.ORDER_STATE_TEMP_RESERVED://state--30
                holder.tvParkingState.setVisibility(View.INVISIBLE);

                time = START_TIME_FORMAT.format(orderListBean.getStartTime()) + "-" + END_TIME_FORMAT.format(orderListBean.getEndTime());
                fee = "用户已预约";
                break;
            case Constant.ORDER_STATE_RESERVED://state--31
                holder.tvParkingState.setVisibility(View.INVISIBLE);

                time = START_TIME_FORMAT.format(orderListBean.getStartTime()) + "-" + END_TIME_FORMAT.format(orderListBean.getEndTime());
                fee = "用户已预约";
                break;
            case Constant.ORDER_STATE_PARKED://state--32
                holder.tvParkingState.setVisibility(View.INVISIBLE);

                time = START_TIME_FORMAT.format(orderListBean.getEnterTime()) + "-" + END_TIME_FORMAT.format(orderListBean.getEndTime());
                fee = "用户正在使用...";
                break;
            case Constant.ORDER_STATE_NOT_PAID://state--33
                holder.tvParkingState.setVisibility(View.INVISIBLE);

                time = START_TIME_FORMAT.format(orderListBean.getEnterTime()) + "-" + END_TIME_FORMAT.format(orderListBean.getLeaveTime());
                fee = "收取费用：" + String.format(Locale.CHINA, DECIMAL_2, (float) orderListBean.getOwnerFee());
                break;
            case Constant.ORDER_STATE_PAID://state--34
                holder.tvParkingState.setVisibility(View.INVISIBLE);

                time = START_TIME_FORMAT.format(orderListBean.getEnterTime()) + "-" + END_TIME_FORMAT.format(orderListBean.getLeaveTime());
                fee = "收取费用：" + String.format(Locale.CHINA, DECIMAL_2, (float) orderListBean.getOwnerFee());
                break;
            case Constant.ORDER_STATE_TIMEOUT://state--38
                holder.tvParkingState.setVisibility(View.VISIBLE);

                time = START_TIME_FORMAT.format(orderListBean.getStartTime());
                fee = "超时补偿：" + String.format(Locale.CHINA, DECIMAL_2, (float) orderListBean.getOwnerFee());
                break;
            default:
                break;
        }

        holder.tvTime.setText(time);
        holder.tvFee.setText(fee);
    }

    @Override
    public int getItemCount() {
        return mOrderBeanList.size();
    }

    static class OrderOwnerViewHolder extends RecyclerView.ViewHolder{

        TextView tvParkingName;
        TextView tvParkingState;
        TextView tvTime;
        TextView tvOrderId;
        TextView tvFee;

        public OrderOwnerViewHolder(View itemView) {
            super(itemView);
            tvParkingName = (TextView) itemView.findViewById(R.id.tv_item_order_owner_parking);
            tvParkingState = (TextView) itemView.findViewById(R.id.tv_item_order_owner_state);
            tvTime = (TextView) itemView.findViewById(R.id.tv_item_order_owner_time);
            tvOrderId = (TextView) itemView.findViewById(R.id.tv_item_order_owner_id);
            tvFee = (TextView) itemView.findViewById(R.id.tv_item_order_owner_fee);
        }
    }
}
