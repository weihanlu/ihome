package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.nfc.Tag;
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
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
        LogUtil.i("ViewHolder", "position is " + position);
        OrderResponse.DataBean.OrderListBean orderListBean = mOrderBeanList.get(position);

        String parkingName = orderListBean.getEstate().getName();
        String orderId = "订单号：" + String.valueOf(orderListBean.getId());
        String time = "";
        String feeInfo = "";
        String state = "";

        int orderState = orderListBean.getState();
        switch (orderState) {
            case Constant.ORDER_STATE_CANCEL:
                time = START_TIME_FORMAT.format(orderListBean.getStartTime())
                        + "-" + END_TIME_FORMAT.format(orderListBean.getEndTime());
                feeInfo = "订单已取消";
                state = "已取消";
                holder.tv_state.setBackground(ContextCompat.getDrawable(mContext, R.drawable.gray_rect_reserve));
                break;
            case Constant.ORDER_STATE_PAID:
                time = START_TIME_FORMAT.format(orderListBean.getEnterTime())
                        + "-" + END_TIME_FORMAT.format(orderListBean.getLeaveTime());
                feeInfo = "已支付：" + String.format(Locale.CHINA, DECIMAL_2, (float) orderListBean.getPayFee()) + "元";
                state = "已完成";
                holder.tv_state.setBackground(ContextCompat.getDrawable(mContext, R.drawable.gray_rect_reserve));
                break;
            case Constant.ORDER_STATE_TIMEOUT:
                time = START_TIME_FORMAT.format(orderListBean.getStartTime())
                        + "-" + END_TIME_FORMAT.format(orderListBean.getEndTime());
                feeInfo = "超时已扣除：" + String.format(Locale.CHINA, DECIMAL_2, (float) orderListBean.getPayFee()) + "元（担保费）";
                state = "已超时";
                holder.tv_state.setBackground(ContextCompat.getDrawable(mContext, R.drawable.gray_rect_reserve));
                break;
            case Constant.ORDER_STATE_NOT_PAID:
                time = START_TIME_FORMAT.format(orderListBean.getEnterTime()) + "-" + END_TIME_FORMAT.format(orderListBean.getLeaveTime());
                feeInfo = "待支付金额：" + String.format(Locale.CHINA, DECIMAL_2, orderListBean.getPayFee());
                state = "未支付";
                holder.tv_state.setBackground(ContextCompat.getDrawable(mContext, R.drawable.blue_rect_reserve));
                break;
            case Constant.ORDER_STATE_TEMP_RESERVED:
                feeInfo = "待支付担保费：" + String.format(Locale.CHINA, DECIMAL_2, orderListBean.getPayFee());
                state = "未支付";
                holder.tv_state.setBackground(ContextCompat.getDrawable(mContext, R.drawable.blue_rect_reserve));
                break;
            default:
                break;
        }

        holder.tv_parking_name.setText(parkingName);
        holder.tv_order_id.setText(orderId);
        holder.tv_fee.setText(feeInfo);
        holder.tv_state.setText(state);

        if (orderState == Constant.ORDER_STATE_TEMP_RESERVED) {
            mCountDownTimer = new MyCountDownTimer(orderListBean.getCreateTime() + 15 * 60 * 1000 - System.currentTimeMillis(), INTERVAL, holder.tv_time);
            mCountDownTimer.start();
        } else {
            holder.tv_time.setText(time);
        }

        if (orderState == Constant.ORDER_STATE_NOT_PAID || orderState == Constant.ORDER_STATE_TEMP_RESERVED) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // do nothing
                }
            });
        }
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

        private TextView mTvTime;

        public MyCountDownTimer(long millisInFuture, long countDownInterval, TextView textView) {
            super(millisInFuture, countDownInterval);
            mTvTime = textView;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long time = millisUntilFinished / 1000;
            LogUtil.i("ViewHolder", "time is " + time);
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

    public void setOnItemClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onClick(View view, int i);
    }

    public void cancelTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }
}
