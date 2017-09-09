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

public class QhDeleteItemDialog extends Dialog{

    private Context mContext;

    private String mTitle;
    private int mType = 0;

    public QhDeleteItemDialog(@NonNull Context context) {
        super(context, R.style.qh_dialog_Theme);
        mContext = context;
    }

    public QhDeleteItemDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    public QhDeleteItemDialog(@NonNull Context context, String mTitle, int mType) {
        super(context, R.style.qh_dialog_Theme);
        this.mContext = context;
        this.mTitle = mTitle;
        this.mType = mType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_delete_item, null);

        setContentView(view);

        if (mType == 1){
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_delete_title);
            tvTitle.setText(mTitle);
        }

        TextView mTvQuitCallback = (TextView) view.findViewById(R.id.tv_quit_callback);
        mTvQuitCallback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QhDeleteItemDialog.this.dismiss();
            }
        });

        TextView mTvSureCallback = (TextView) view.findViewById(R.id.tv_sure_callback);
        mTvSureCallback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSure(v);
                }
                QhDeleteItemDialog.this.dismiss();
            }
        });
    }

    public interface OnSureCallbackListener {
        void onSure(View view);
    }

    private OnSureCallbackListener mListener;

    public void setOnSureCallbackListener(OnSureCallbackListener listener) {
        this.mListener = listener;
    }
}
