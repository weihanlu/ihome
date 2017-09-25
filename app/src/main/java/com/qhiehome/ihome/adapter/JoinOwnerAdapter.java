package com.qhiehome.ihome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.qhiehome.ihome.R;

/**
 * Created by YueMa on 2017/9/25.
 */

public class JoinOwnerAdapter extends RecyclerView.Adapter<JoinOwnerAdapter.JoinOwnerViewHolder> {


    private Context mContext;
    private String[] mTitles;
    private String[] mHints;
    private String[] mContent = new String[10];

    public JoinOwnerAdapter(Context mContext, String[] mTitles, String[] mHints) {
        this.mContext = mContext;
        this.mTitles = mTitles;
        this.mHints = mHints;
    }

    @Override
    public JoinOwnerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_join_owner, parent, false);
        return new JoinOwnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JoinOwnerViewHolder holder, final int position) {
        holder.tvTitle.setText(mTitles[position]);
        holder.etContent.setHint(mHints[position]);
        holder.etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mContent[position] = s.toString().trim();
            }
        });
    }

    @Override
    public int getItemCount() {

        return mTitles.length;
    }

    class JoinOwnerViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        EditText etContent;

        public JoinOwnerViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_join_title);
            etContent = (EditText) itemView.findViewById(R.id.et_join);
        }
    }

    public String[] getmContent() {
        return mContent;
    }
}
