package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.util.Constant;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by YueMa on 2017/9/8.
 */

public class ReserveListAdapter extends RecyclerView.Adapter<ReserveListAdapter.ReserveViewHolder>{

    private List<OrderResponse.DataBean.OrderListBean> mOrderBeanList;
    private Context mContext;

    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final String DECIMAL_2 = "%.2f";
    private static final long INTERVAL = 1000L;

    private MyCountDownTimer mCountDownTimer;

    public ReserveListAdapter(List<OrderResponse.DataBean.OrderListBean> mOrderBeanList, Context mContext) {
        this.mOrderBeanList = mOrderBeanList;
        this.mContext = mContext;
    }

    public void setmOrderBeanList(List<OrderResponse.DataBean.OrderListBean> mOrderBeanList) {
        this.mOrderBeanList = mOrderBeanList;
    }

    @Override
    public ReserveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_reserve_header, parent, false);

        return new ReserveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReserveViewHolder holder, int position) {
        holder.tv_parking_name.setText(mOrderBeanList.get(position).getEstate().getName());
        holder.tv_order_id.setText("订单号:" + mOrderBeanList.get(position).getId());
        String time = "";
        String feeInfo = "";
        switch (mOrderBeanList.get(position).getState()) {
            case Constant.ORDER_STATE_TEMP_RESERVED:
                feeInfo = "待支付担保费：" + String.format(Locale.CHINA, DECIMAL_2, mOrderBeanList.get(position).getPayFee());
                long timeRemaining = mOrderBeanList.get(0).getCreateTime() + 15*60*1000 - System.currentTimeMillis();
                if (mCountDownTimer == null) {
                    mCountDownTimer = new MyCountDownTimer(timeRemaining, INTERVAL, holder.tv_time);
                }
                mCountDownTimer.start();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickListener != null) {
                            onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                        }
                    }
                });
                holder.tv_state.setText("未支付");
                holder.tv_state.setBackground(ContextCompat.getDrawable(mContext, R.drawable.blue_rect_reserve));
                break;
            case Constant.ORDER_STATE_NOT_PAID:
                feeInfo = "待支付金额：" + String.format(Locale.CHINA, DECIMAL_2, mOrderBeanList.get(position).getPayFee());
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getEnterTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getLeaveTime());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickListener != null) {
                            onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                        }
                    }
                });
                holder.tv_state.setText("未支付");
                holder.tv_state.setBackground(ContextCompat.getDrawable(mContext, R.drawable.blue_rect_reserve));
                break;
            case Constant.ORDER_STATE_CANCEL:
                feeInfo = "订单已取消";
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getStartTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getEndTime());
                holder.tv_state.setText("已取消");
                break;
            case Constant.ORDER_STATE_PAID:
                feeInfo = "已支付：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(position).getPayFee()) + "元";
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getEnterTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getLeaveTime());
                holder.tv_state.setText("已完成");
                break;
            case Constant.ORDER_STATE_TIMEOUT:
                feeInfo = "超时已扣除：" + String.format(Locale.CHINA, DECIMAL_2, (float) mOrderBeanList.get(position).getPayFee()) + "元（担保费）";
                time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getStartTime()) + "-" + END_TIME_FORMAT.format(mOrderBeanList.get(position).getEndTime());
                holder.tv_state.setText("已超时");
                break;
            default:
                break;
        }
        holder.tv_fee.setText(feeInfo);
        holder.tv_time.setText(time);
    }

    @Override
    public int getItemCount() {
        return mOrderBeanList.size();
    }

    static class ReserveViewHolder extends RecyclerView.ViewHolder{

        TextView tv_parking_name;
        TextView tv_time;
        TextView tv_order_id;
        TextView tv_fee;
        TextView tv_state;

        public ReserveViewHolder(View itemView) {
            super(itemView);
            tv_parking_name = (TextView) itemView.findViewById(R.id.tv_item_reserve_parking);
            tv_time = (TextView) itemView.findViewById(R.id.tv_item_reserve_time);
            tv_order_id = (TextView) itemView.findViewById(R.id.tv_item_reserve_orderid);
            tv_fee = (TextView) itemView.findViewById(R.id.tv_item_reserve_fee);
            tv_state = (TextView) itemView.findViewById(R.id.tv_item_reserve_state);

        }
    }

    public class MyCountDownTimer extends CountDownTimer {

        TextView mTvTime;

        public MyCountDownTimer(long millisInFuture, long countDownInterval, TextView tv_time) {
            super(millisInFuture, countDownInterval);
            mTvTime = tv_time;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long time = millisUntilFinished / 1000;

            if (time <= 59) {
                mTvTime.setText(String.format(Locale.CHINA, "剩余支付时间：00:%02d", time));
            } else {
                mTvTime.setText(String.format(Locale.CHINA, "剩余支付时间：%02d:%02d", time / 60, time % 60));
            }
        }

        @Override
        public void onFinish() {
            mTvTime.setText("剩余支付时间：00:00");
            cancelTimer();
        }

    }


    public void cancelTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    public void setOnItemClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onClick(View view, int i);
    }
}
