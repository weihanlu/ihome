package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.model.inquiry.order.OrderResponse;
import com.qhiehome.ihome.network.model.inquiry.orderowner.OrderOwnerResponse;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by YueMa on 2017/8/25.
 */

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {

    private Context mContext;

    private List<OrderOwnerResponse.DataBean.OrderListBean> mOrderBeanList;

    private static final SimpleDateFormat START_TIME_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final String DECIMAL_2 = "%.2f";

    public OrderListAdapter(Context context, List<OrderOwnerResponse.DataBean.OrderListBean> orderBeanList) {
        this.mContext = context;
        this.mOrderBeanList = orderBeanList;
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        String name = mOrderBeanList.get(position).getEstate().getName();
        name += " - ";
        name += mOrderBeanList.get(position).getParking().getName();
        holder.tv_estate.setText(name);
        String time = START_TIME_FORMAT.format(mOrderBeanList.get(position).getEnterTime());
        time += " - ";
        time += END_TIME_FORMAT.format(mOrderBeanList.get(position).getLeaveTime());
        holder.tv_time.setText(time);
        String fee = String.format(Locale.CHINA, DECIMAL_2,(float) mOrderBeanList.get(position).getOwnerFee());
        fee += "元";
        holder.tv_fee.setText(fee);
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_order_list, parent, false);

        return new OrderViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mOrderBeanList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder{

        TextView tv_estate;
        TextView tv_time;
        TextView tv_fee;

        public OrderViewHolder(View itemView) {
            super(itemView);
            tv_estate = (TextView) itemView.findViewById(R.id.tv_order_estate);
            tv_time = (TextView) itemView.findViewById(R.id.tv_order_time);
            tv_fee = (TextView) itemView.findViewById(R.id.tv_order_fee);
        }
    }
}
