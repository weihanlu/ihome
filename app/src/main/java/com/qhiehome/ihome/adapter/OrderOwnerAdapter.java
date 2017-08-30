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

    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final String DECIMAL_2 = "%.2f";


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
        String parking = mOrderBeanList.get(position).getEstate().getName() + "-" + mOrderBeanList.get(position).getParking().getName();
        holder.tv_parking.setText(parking);
        String time = "";
        String fee = "";
        switch (mOrderBeanList.get(position).getState()){
            case Constant.ORDER_STATE_TEMP_RESERVED://state--30
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getStartTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getEndTime());
                fee = "用户已预约";
                break;
            case Constant.ORDER_STATE_RESERVED://state--31
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getStartTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getEndTime());
                fee = "用户已预约";
                break;
            case Constant.ORDER_STATE_PARKED://state--32
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getEnterTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getEndTime());
                fee = "用户正在使用...";
                break;
            case Constant.ORDER_STATE_NOT_PAID://state--33
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getEnterTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getLeaveTime());
                fee = "收取费用：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(position).getOwnerFee());
                break;
            case Constant.ORDER_STATE_PAID://state--34
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getEnterTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getLeaveTime());
                fee = "收取费用：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(position).getOwnerFee());
                break;
            case Constant.ORDER_STATE_TIMEOUT://state--38
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getStartTime()) + "（超时未停车）";
                fee = "超时补偿：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(position).getOwnerFee());
                break;
            default:
                break;
        }
        String orderId = "订单号：" + String.format(Locale.CHINA, "%d", mOrderBeanList.get(position).getId());
        holder.tv_orderId.setText(orderId);
        holder.tv_time.setText(time);
        holder.tv_fee.setText(fee);
    }

    @Override
    public int getItemCount() {
        return mOrderBeanList.size();
    }

    static class OrderOwnerViewHolder extends RecyclerView.ViewHolder{

        TextView tv_parking;
        TextView tv_orderId;
        TextView tv_time;
        TextView tv_fee;

        public OrderOwnerViewHolder(View itemView) {
            super(itemView);
            tv_parking = (TextView) itemView.findViewById(R.id.tv_item_order_owner_parking);
            tv_orderId = (TextView) itemView.findViewById(R.id.tv_item_order_owner_id);
            tv_time = (TextView) itemView.findViewById(R.id.tv_item_order_owner_time);
            tv_fee = (TextView) itemView.findViewById(R.id.tv_item_order_owner_fee);
        }
    }
}
