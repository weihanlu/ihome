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
import com.qhiehome.ihome.adapter.PublishParkingAdapter;

public class QhAvatarSelectDialog extends Dialog{

    private Context mContext;

    private String mSelect1;
    private String mSelect2;
    private int mType = 0;  //0:选择头像; 1:选择支付担保费or取消预约

    public QhAvatarSelectDialog(@NonNull Context context) {
        super(context, R.style.qh_dialog_Theme);
        mContext = context;
    }

    public QhAvatarSelectDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    public QhAvatarSelectDialog(@NonNull Context context, String mSelect1, String mSelect2, int mType) {
        super(context, R.style.qh_dialog_Theme);
        this.mContext = context;
        this.mSelect1 = mSelect1;
        this.mSelect2 = mSelect2;
        this.mType = mType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_avatar_select, null);
        setContentView(view);

        TextView mTakePhoto = (TextView) view.findViewById(R.id.tv_take_photo);

        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onTakePhoto(v);
                }
                QhAvatarSelectDialog.this.dismiss();
            }
        });

        TextView mGallery = (TextView) view.findViewById(R.id.tv_gallery);
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onGallery(v);
                }
                QhAvatarSelectDialog.this.dismiss();
            }
        });

        if (mType == 1){
            mTakePhoto.setText(mSelect1);
            mGallery.setText(mSelect2);
        }
    }

    public interface OnItemClickListener {
        void onTakePhoto(View view);
        void onGallery(View view);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }
}
