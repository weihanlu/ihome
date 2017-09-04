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

    public QhAvatarSelectDialog(@NonNull Context context) {
        super(context, R.style.qh_dialog_Theme);
        mContext = context;
    }

    public QhAvatarSelectDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
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
