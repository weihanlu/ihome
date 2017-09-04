package com.qhiehome.ihome.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.qhiehome.ihome.R;

public class QhPublishParkingDialog extends Dialog{

    private Context mContext;

    private View mView;

    public QhPublishParkingDialog(@NonNull Context context) {
        super(context, R.style.qh_dialog_Theme);
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_publish_parking, null);
    }

    public QhPublishParkingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(mView);

        TextView mTvQuitPublish = (TextView) mView.findViewById(R.id.tv_quit_publish);
        mTvQuitPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QhPublishParkingDialog.this.dismiss();
            }
        });

        TextView mTvSurePublish = (TextView) mView.findViewById(R.id.tv_sure_publish);
        mTvSurePublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSure(v);
                }
                QhPublishParkingDialog.this.dismiss();
            }
        });
    }

    public interface OnSurePublishListener {
        void onSure(View view);
    }

    private OnSurePublishListener mListener;

    public void setOnSurePublishListener(OnSurePublishListener listener) {
        this.mListener = listener;
    }

    public View getCustomView() {
        return mView;
    }
}
