package com.qhiehome.ihome.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;

public class QhLockConnectDialog extends Dialog{

    private Context mContext;

    public QhLockConnectDialog(@NonNull Context context) {
        super(context, R.style.qh_dialog_Theme);
        mContext = context;
    }

    public QhLockConnectDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_lock_connect, null);
        setContentView(view);

        ImageView mIvLockUp = (ImageView) view.findViewById(R.id.iv_lock_up);
        mIvLockUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onLockUp(v);
                }
            }
        });

        ImageView mIvLockDown = (ImageView) view.findViewById(R.id.iv_lock_down);
        mIvLockDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onLockDown(v);
                }
            }
        });

        TextView mTvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QhLockConnectDialog.this.dismiss();
            }
        });
    }

    public interface OnItemClickListener {
        void onLockUp(View view);
        void onLockDown(View view);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }
}
