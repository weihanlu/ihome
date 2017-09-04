package com.qhiehome.ihome.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.util.CommonUtil;

public class QhModifyPasswordDialog extends Dialog{

    private Context mContext;

    private View mView;

    public QhModifyPasswordDialog(@NonNull Context context) {
        super(context, R.style.qh_dialog_Theme);
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_modify_password, null);
    }

    public QhModifyPasswordDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(mView);

        final EditText mEtOldPassword = (EditText) mView.findViewById(R.id.et_old_password);
        final EditText mEtNewPassword = (EditText) mView.findViewById(R.id.et_new_password);
        final EditText mEtConfirmPassword = (EditText) mView.findViewById(R.id.et_confirm_password);


        TextView mTvCancel = (TextView) mView.findViewById(R.id.tv_cancel);
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QhModifyPasswordDialog.this.dismiss();
            }
        });

        TextView mTvConfirm = (TextView) mView.findViewById(R.id.tv_confirm);
        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCofirm(mEtOldPassword.getText().toString(),
                                        mEtNewPassword.getText().toString(),
                                        mEtConfirmPassword.getText().toString());
                }
                QhModifyPasswordDialog.this.dismiss();
            }
        });

        RelativeLayout mRlOldPassword = (RelativeLayout) mView.findViewById(R.id.rl_old_password);
        final View seperatorOldPassword = mView.findViewById(R.id.separator_old_password);
        mRlOldPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtOldPassword.requestFocus();
            }
        });

        RelativeLayout mRlNewPassword = (RelativeLayout) mView.findViewById(R.id.rl_new_password);
        final View seperatorNewPassword = mView.findViewById(R.id.separator_new_password);
        mRlOldPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtNewPassword.requestFocus();
            }
        });

        RelativeLayout mRlConfirmPassword = (RelativeLayout) mView.findViewById(R.id.rl_confirm_password);
        final View seperatorConfirmPassword = mView.findViewById(R.id.separator_confirm_password);
        mRlOldPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtConfirmPassword.requestFocus();
            }
        });

        CommonUtil.showSoftKeyboard(mEtOldPassword, mContext);
    }

    public interface OnItemClickListener {
        void onCofirm(String oldPassword, String newPassword, String confirmPassword);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public View getCustomView() {
        return mView;
    }
}
